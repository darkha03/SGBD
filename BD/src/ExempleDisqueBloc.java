public class ExempleDisqueBloc {

    public static void main(String[] args) {
        DisqueBloc disqueBloc = new DisqueBloc("maTable", 4, 10);
        FullScanDisqueBloc fullScan = new FullScanDisqueBloc(disqueBloc);
        disqueBloc.genereTable();
        fullScan.open();
        Tuple t;
        while ((t = fullScan.next()) != null) {
            System.out.println(t);
        }
        fullScan.close();
        /*
         * IndexHachageStatique indexStatHachage = new IndexHachageStatique();
         * try {
         * indexStatHachage.writeIndex(disqueBloc);
         * } catch (java.security.NoSuchAlgorithmException e) {
         * e.printStackTrace();
         * }
         */
        IndexHachageDynamique indexDynHachage = new IndexHachageDynamique();
        try {
            indexDynHachage.writeIndex(disqueBloc);
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}