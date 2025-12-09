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
        IndexHachageDynamique indexHachage = new IndexHachageDynamique();
        try {
            indexHachage.writeIndex(disqueBloc);
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
}