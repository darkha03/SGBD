import java.io.*;
import java.util.*;
import java.security.MessageDigest;

public class IndexHachageStatique {

    private Map<Integer, Integer> buckets = new HashMap<>(16);

    private int hachage(int key) throws java.security.NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Integer.toString(key).getBytes());
        byte[] digest = md.digest();
        int hashValue = Byte.toUnsignedInt(digest[0]);
        int index = hashValue % 16;
        return index;
    }

    public int getDisqueBlocIndex(int key) throws java.security.NoSuchAlgorithmException {
        return hachage(key);
    }

    public void writeIndex(DisqueBloc disqueBloc) throws java.security.NoSuchAlgorithmException {
        String nomFichier = disqueBloc.getNomFichier();
        int maxTuples = disqueBloc.getMaxTuples();
        List<Tuple> tuples = new ArrayList<>();

        // Lire tous les tuples du DisqueBloc
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            tuples.add(t);
        }
        fullScan.close();

        // Distribuer les tuples dans les buckets
        for (Tuple tuple : tuples) {
            int key = tuple.val[0];
            int index = hachage(key);

            System.out.println("Tuple with key " + key + " goes to index " + index);
            String nomIndexBloc = nomFichier + ".index.bloc" + index;
            buckets.put(index, buckets.getOrDefault(index, 0) + 1);
            if (buckets.get(index) > maxTuples) {
                System.out.println("Warning: Bucket " + index + " exceeds capacity!");
            }
            int nextIndex = buckets.get(index) / maxTuples;
            if (nextIndex > 0) {
                System.out.println("Bucket " + index + " needs overflow block: " + nextIndex);
                nomIndexBloc = nomFichier + ".index.bloc" + index + "." + nextIndex;
            } 
            
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
