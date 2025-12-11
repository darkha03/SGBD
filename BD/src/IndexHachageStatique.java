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

        // Organiser les tuples par bucket
        Map<String, List<Tuple>> bucketTuples = new HashMap<>();
        for (Tuple tuple : tuples) {
            int key = tuple.val[0];
            int index = hachage(key);

            System.out.println("Tuple with key " + key + " goes to index " + index);
            buckets.put(index, buckets.getOrDefault(index, 0) + 1);
            // Déterminer le bloc (principal ou overflow)
            int tupleCount = buckets.get(index);
            int overflowBlock = (tupleCount - 1) / maxTuples;
            String nomIndexBloc;
            if (overflowBlock == 0) {
                nomIndexBloc = nomFichier + ".index.bloc" + index;
            } else {
                nomIndexBloc = nomFichier + ".index.bloc" + index + "." + overflowBlock;
                if (tupleCount > maxTuples) {
                    System.out.println("Warning: Bucket " + index + " exceeds capacity!");
                }
            }
            bucketTuples.computeIfAbsent(nomIndexBloc, k -> new ArrayList<>()).add(tuple);
        }
        for (Map.Entry<String, List<Tuple>> entry : bucketTuples.entrySet()) {
            String nomIndexBloc = entry.getKey();
            List<Tuple> bucketList = entry.getValue();
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/" + nomIndexBloc))) {
                int nombreColonnes = bucketList.isEmpty() ? 0 : bucketList.get(0).size;
                dos.writeInt(nombreColonnes);
                dos.writeInt(bucketList.size());
                dos.writeInt(0); // prochainBloc = 0 (pas de bloc suivant)
                for (Tuple tuple : bucketList) {
                    for (int i = 0; i < tuple.size; i++) {
                        dos.writeInt(tuple.val[i]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
