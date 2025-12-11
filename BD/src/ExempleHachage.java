import java.util.*;

public class ExempleHachage {

    public static void main(String[] args) {
        DisqueBloc disqueBloc = new DisqueBloc("maTableHachage", 4, 30);
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        disqueBloc.genereTable();
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            System.out.println(t);
        }
        fullScan.close();

        IndexHachageDynamique indexDynHachage = new IndexHachageDynamique();
        try {
            indexDynHachage.writeIndex(disqueBloc);
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Affichage des contenus des buckets avec FullScanDisqueBloc
        java.util.Set<Integer> displayedBuckets = new java.util.HashSet<>();
        for (int i = 0; i < indexDynHachage.getDirectorySize(); i++) {
            int bucketId = indexDynHachage.getBucketId(i);

            // Scanner chaque bucket unique une seule fois
            if (!displayedBuckets.contains(bucketId)) {
                System.out.println("\nBucket ID: " + bucketId);
                DisqueBloc indexDisqueBloc = indexDynHachage.getDisqueBloc(i);
                FullScanDisqueBloc indexFullScan = new FullScanDisqueBloc(indexDisqueBloc, bucketId);
                indexFullScan.open();
                Tuple indexTuple;
                while ((indexTuple = indexFullScan.next()) != null) {
                    System.out.println("  " + indexTuple);
                }
                indexFullScan.close();
                displayedBuckets.add(bucketId);
            }
        }
    }
}
