public class ExempleJointure {
    
    public static void main(String[] args) {
        int tailleTuple = 3;
        DisqueBloc source1 = new DisqueBloc("source1", tailleTuple, 20);
        DisqueBloc source2 = new DisqueBloc("source2", tailleTuple, 20);
        
        source1.genereTable();
        source2.genereTable();

        FullScanDisqueBloc opGauche = new FullScanDisqueBloc(source1);
        FullScanDisqueBloc opDroit = new FullScanDisqueBloc(source2);
        
        IndexHachageDynamique indexHachage = new IndexHachageDynamique();
        try {
            indexHachage.writeIndex(source2);
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        JointureSurIndex jointure = new JointureSurIndex(opGauche, opDroit, indexHachage, 0, 0);
        
        jointure.open();
        Tuple temp = null;
        while((temp = jointure.next()) != null) {
            System.out.println(temp);
        }
        jointure.close();
        
        System.out.println("******************");
        
    }
}
