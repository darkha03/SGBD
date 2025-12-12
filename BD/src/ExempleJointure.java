public class ExempleJointure {

    public static void main(String[] args) {
        int tailleTuple = 3;
        DisqueBloc source1 = new DisqueBloc("source1", tailleTuple, 20);
        DisqueBloc source2 = new DisqueBloc("source2", tailleTuple, 20);

        source1.genereTable();
        source2.genereTable();

        System.out.println("TABLE 1 : ");
        Operateur scan1_display = new FullScanDisqueBloc(source1);
        scan1_display.open();
        Tuple t;
        while ((t = scan1_display.next()) != null) {
            System.out.println(t);
        }
        scan1_display.close();
        System.out.println("TABLE 2 : ");
        Operateur scan2_display = new FullScanDisqueBloc(source2);
        scan2_display.open();
        while ((t = scan2_display.next()) != null) {
            System.out.println(t);
        }
        scan2_display.close();

        FullScanDisqueBloc opGauche = new FullScanDisqueBloc(source1);
        FullScanDisqueBloc opDroit = new FullScanDisqueBloc(source2);

        IndexHachageDynamique indexHachage = new IndexHachageDynamique();
        try {
            indexHachage.writeIndex(source2);
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("RÉSULTAT DE LA JOINTURE");
        JointureSurIndex jointure = new JointureSurIndex(opGauche, opDroit, indexHachage, 0, 0);

        jointure.open();
        Tuple temp = null;
        while ((temp = jointure.next()) != null) {
            System.out.println(temp);
        }
        jointure.close();

        System.out.println("******************");

    }
}
