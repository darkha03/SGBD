import java.io.*;
import java.util.*;
import java.security.MessageDigest;

public class IndexHachageStatique {

    private Map<Integer, List<Tuple>> buckets = new HashMap<>();

    private int hachage(int key) throws java.security.NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Integer.toString(key).getBytes());
        byte[] digest = md.digest();
        int hashValue = Byte.toUnsignedInt(digest[0]);
        int index = hashValue % 3;
        return index;
    }

    public int getDisqueBlocIndex(int key) throws java.security.NoSuchAlgorithmException {
        return hachage(key);
    }

    public void writeIndex(DisqueBloc disqueBloc) throws java.security.NoSuchAlgorithmException {
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
            int key = tuple.val[0];
            int index = hachage(key);
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
