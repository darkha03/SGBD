import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IndexHachageDynamique {
    private class Bucket {
        int localDepth;
        List<Tuple> tuples;
        int id;
        private static int counter = 0;

        public Bucket(int depth) {
            this.localDepth = depth;
            this.tuples = new ArrayList<>();
            this.id = counter++;
        }
    }

    private List<Bucket> directory;
    private int globalDepth;
    private int maxTuples;
    private String nomFichier;

    public IndexHachageDynamique() {
        this.directory = new ArrayList<>();
        this.globalDepth = 1;
        directory.add(new Bucket(1));
        directory.add(new Bucket(1));
    }

    private int hachage(int key, int depth) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Integer.toString(key).getBytes());
        byte[] digest = md.digest();
        int hashValue = Byte.toUnsignedInt(digest[0]);
        int mask = (1 << depth) - 1;
        return hashValue & mask;
    }

    public int getDisqueBlocIndex(int key, int depth) throws NoSuchAlgorithmException {
        return hachage(key, depth);
    }

    public DisqueBloc getDisqueBloc(int index) {
        Bucket bucket = directory.get(index);
        String nomIndexBase = nomFichier + ".index";
        return new DisqueBloc(nomIndexBase, maxTuples, 0);
    }

    public int getBucketId(int index) {
        Bucket bucket = directory.get(index);
        return bucket.id;
    }

    public int getGlobalDepth() {
        return globalDepth;
    }

    public int getDirectorySize() {
        return directory.size();
    }

    public void writeIndex(DisqueBloc disqueBloc) throws NoSuchAlgorithmException {
        nomFichier = disqueBloc.getNomFichier();
        this.maxTuples = disqueBloc.getMaxTuples();

        // Lire tous les tuples du DisqueBloc
        List<Tuple> allTuples = new ArrayList<>();
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            allTuples.add(t);
        }
        fullScan.close();

        // Distribuer les tuples dans les buckets
        for (Tuple tuple : allTuples) {
            insertTuple(tuple);
        }

        Set<Bucket> processedBuckets = new HashSet<>();

        for (int i = 0; i < directory.size(); i++) {
            Bucket bucket = directory.get(i);

            if (processedBuckets.contains(bucket)) {
                continue;
            }

            String nomIndexBloc = nomFichier + ".index.bloc" + bucket.id;

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/" + nomIndexBloc))) {
                // Écrire les en-têtes du bucket
                int nombreColonnes = bucket.tuples.isEmpty() ? 0 : bucket.tuples.get(0).size;
                dos.writeInt(nombreColonnes);
                dos.writeInt(bucket.tuples.size());
                dos.writeInt(0); // prochainBloc = 0 (pas de bloc suivant)

                // Écrire les tuples
                for (Tuple tuple : bucket.tuples) {
                    for (int k = 0; k < tuple.size; k++) {
                        dos.writeInt(tuple.val[k]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            processedBuckets.add(bucket);
        }
    }

    private void insertTuple(Tuple tuple) throws NoSuchAlgorithmException {
        int key = tuple.val[0];
        int dirIndex = hachage(key, globalDepth);
        System.out.println("Tuple with key " + key + " goes to directory with index " + dirIndex);
        Bucket bucket = directory.get(dirIndex);
        if (bucket.tuples.size() < this.maxTuples) {
            bucket.tuples.add(tuple);
        } else {
            splitBucket(bucket, tuple);
        }
    }

    private void splitBucket(Bucket oldBucket, Tuple pendingTuple) throws NoSuchAlgorithmException {
        if (oldBucket.localDepth == globalDepth) {
            doubleDirectory();
        }
        oldBucket.localDepth++;
        Bucket newBucket = new Bucket(oldBucket.localDepth);
        List<Tuple> allTuples = new ArrayList<>(oldBucket.tuples);
        allTuples.add(pendingTuple);
        oldBucket.tuples.clear();
        int indexDiffBit = 1 << (oldBucket.localDepth - 1);
        for (int i = 0; i < directory.size(); i++) {
            if (directory.get(i) == oldBucket) {
                if ((i & indexDiffBit) != 0) {
                    directory.set(i, newBucket);
                }
            }
        }
        for (Tuple t : allTuples) {
            insererApresSplit(t);
        }
    }

    private void insererApresSplit(Tuple tuple) throws NoSuchAlgorithmException {
        int dirIndex = hachage(tuple.val[0], globalDepth);
        Bucket bucket = directory.get(dirIndex);
        if (bucket.tuples.size() < this.maxTuples) {
            bucket.tuples.add(tuple);
        } else {
            splitBucket(bucket, tuple);
        }
    }

    private void doubleDirectory() {
        int currentSize = directory.size();
        for (int i = 0; i < currentSize; i++) {
            directory.add(directory.get(i));
        }
        globalDepth++;
    }
}