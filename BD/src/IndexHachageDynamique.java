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

    private List<Bucket> directory = new ArrayList<>();
    private int globalDepth = 1;
    private final int MAX_TUPLES = 4;

    public IndexHachageDynamique() {
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

    public void writeIndex(DisqueBloc disqueBloc) throws NoSuchAlgorithmException {
        String nomFichier = disqueBloc.getNomFichier();
        List<Tuple> tuples = new ArrayList<>();
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            tuples.add(t);
        }
        fullScan.close();
        for (Tuple tuple : tuples) {
            insertTuple(tuple);
        }
        Set<Bucket> processedBuckets = new HashSet<>();
        for (int i = 0; i < directory.size(); i++) {
            Bucket bucket = directory.get(i);

            if (processedBuckets.contains(bucket)) {
                continue;
            }
            String nomIndexBloc = nomFichier + ".index.bucket" + bucket.id;
            try (FileWriter writer = new FileWriter("data/" + nomIndexBloc, false)) {
                for (Tuple tuple : bucket.tuples) {
                    for (int k = 0; k < tuple.size; k++) {
                        writer.write(tuple.val[k] + (k == tuple.size - 1 ? "" : ","));
                    }
                    writer.write("\n");
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
        if (bucket.tuples.size() < MAX_TUPLES) {
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
            int index = hachage(t.val[0], globalDepth);
            directory.get(index).tuples.add(t);
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