public class JointureSurIndex implements Operateur {
    private Operateur operateurGauche;
    private Operateur operateurDroit;
    private IndexHachageDynamique indexHachage;
    private int colonneGauche;
    private int colonneDroit;
    private Tuple tupleCourantGauche;
    private FullScanDisqueBloc scanDroit;

    public JointureSurIndex(Operateur operateurGauche, Operateur operateurDroit,
                            IndexHachageDynamique indexHachage,
                            int colonneGauche, int colonneDroit) {
        this.operateurGauche = operateurGauche;
        this.operateurDroit = operateurDroit;
        this.indexHachage = indexHachage;
        this.colonneGauche = colonneGauche;
        this.colonneDroit = colonneDroit;
    }

    @Override
    public void open() {
        operateurGauche.open();
        operateurDroit.open();
        scanDroit = null;
    }

    @Override
    public Tuple next() {
        while ((tupleCourantGauche = operateurGauche.next()) != null) {
            
            int valeurRecherchee = tupleCourantGauche.val[colonneGauche];
            
            try {
                int blocIndex = indexHachage.getDisqueBlocIndex(valeurRecherchee, indexHachage.getGlobalDepth());
                DisqueBloc disqueBloc = indexHachage.getDisqueBloc(blocIndex);
                int bucketId = indexHachage.getBucketId(blocIndex);
                scanDroit = new FullScanDisqueBloc(disqueBloc, bucketId - 1);
                scanDroit.open();

                Tuple tupleDroit;
                while ((tupleDroit = scanDroit.next()) != null) {
                    if (tupleDroit.val[colonneDroit] == valeurRecherchee) {
                        return combineTuples(tupleCourantGauche, tupleDroit);
                    }
                }
                scanDroit.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Tuple combineTuples(Tuple gauche, Tuple droit) {
        Tuple resultat = new Tuple(gauche.size + droit.size);
        for (int i = 0; i < gauche.size; i++) {
            resultat.val[i] = gauche.val[i];
        }
        for (int j = 0; j < droit.size; j++) {
            resultat.val[gauche.size + j] = droit.val[j];
        }
        return resultat;
    }

    @Override
    public void close() {
        operateurGauche.close();
        if (scanDroit != null) {
            scanDroit.close();
        }
        operateurDroit.close();
    }
}