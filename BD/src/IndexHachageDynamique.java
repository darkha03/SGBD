import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IndexHachageDynamique {

    private Map<Integer, List<Tuple>> buckets;

    private int hachage(int key, int depth) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Integer.toString(key).getBytes());
        byte[] digest = md.digest();
        int hashValue = digest[0] & 0xFF;
        int index = hashValue % (1 << depth);
        return index;
    }
    
    public int getDisqueBlocIndex(int key, int depth) throws NoSuchAlgorithmException {
        return hachage(key, depth);
    }

    public void writeIndex(DisqueBloc disqueBloc) throws NoSuchAlgorithmException {
        buckets = new HashMap<>();
        String nomFichier = disqueBloc.getNomFichier();
        int maxTuples = disqueBloc.getMaxTuples();

        // Lire tous les tuples du DisqueBloc
        List<Tuple> tuples = new ArrayList<>();
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            tuples.add(t);
        }
        fullScan.close();
        int depth = 1;

        // Distribuer les tuples dans les buckets
        for (Tuple tuple : tuples) {
            int key = tuple.val[0];
            int index = hachage(key, depth);
            buckets.putIfAbsent(index, new ArrayList<>());
            buckets.get(index).add(tuple);
            if (buckets.get(index).size() > maxTuples) {
                // Créer une copie pour éviter ConcurrentModificationException
                List<Tuple> currentBucket = new ArrayList<>(buckets.get(index));
                for (Tuple tempTuple : currentBucket) {
                    int newDepth = Integer.toBinaryString(index).length() + 1;
                    int newIndex = hachage(tempTuple.val[0], newDepth);
                    if (newIndex != index) {
                        buckets.putIfAbsent(newIndex, new ArrayList<>());
                        buckets.get(newIndex).add(tempTuple);
                        buckets.get(index).remove(tempTuple);
                    }
                }
            }
        }

        // Écrire les buckets dans les fichiers
        for (Map.Entry<Integer, List<Tuple>> entry : buckets.entrySet()) {
            int index = entry.getKey();
            List<Tuple> bucketTuples = entry.getValue();
            String nomIndexBloc = nomFichier + ".index.bloc" + index;
            try (FileWriter writer = new FileWriter("data/" + nomIndexBloc, true)) {
                for (Tuple tuple : bucketTuples) {
                    for (int i = 0; i < tuple.size; i++) {
                        writer.write(tuple.val[i]);
                    }
                    System.out.println("Written tuple with key " + tuple.val[0] + " to index " + index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
