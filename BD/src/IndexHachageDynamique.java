import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IndexHachageDynamique {

    private Map<Integer, List<Tuple>> buckets = new HashMap<>();

    private int hachage(int key, int depth) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Integer.toString(key).getBytes());
        byte[] digest = md.digest();
        int hashValue = Byte.toUnsignedInt(digest[0]);
        int index = hashValue % 3;
        return index;
    }
    
    public int getDisqueBlocIndex(int key, int depth) throws NoSuchAlgorithmException {
        return hachage(key, depth);
    }

    public void writeIndex(DisqueBloc disqueBloc) throws NoSuchAlgorithmException {
        String nomFichier = disqueBloc.getNomFichier();
        int maxTuples = 4;
        List<Tuple> tuples = new ArrayList<>();
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            tuples.add(t);
        }
        fullScan.close();
        int depth = 1;
        for (Tuple tuple : tuples) {
            int key = tuple.val[0];
            int index = hachage(key, depth);
            buckets.putIfAbsent(index, new ArrayList<>());
            buckets.get(index).add(tuple);
            if (buckets.get(index).size() > maxTuples) {
                depth++;
                Map<Integer, List<Tuple>> newBuckets = new HashMap<>();
                for (List<Tuple> bucketTuples : buckets.values()) {
                    for (Tuple bucketTuple : bucketTuples) {
                        int newIndex = hachage(bucketTuple.val[0], depth);
                        newBuckets.putIfAbsent(newIndex, new ArrayList<>());
                        newBuckets.get(newIndex).add(bucketTuple);
                    }
                }
                buckets = newBuckets;
            }
            System.out.println("Tuple with key " + key + " goes to index " + index);
            String nomIndexBloc = nomFichier + ".index.bloc" + index;
            try (FileWriter writer = new FileWriter( "data/" + nomIndexBloc, true)) {
                for (int i = 0; i < tuple.size; i++) {
                    writer.write(tuple.val[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
