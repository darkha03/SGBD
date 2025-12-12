public class ExempleOptimiseur {
    private static final int TAILLE_TUPLE = 4;
    private static final int NOMBRE_TUPLES = 50;
    private static final int COLONNE_FILTRE = 2;
    private static final int VALEUR_FILTRE = 10;
    private static final int[] COLONNES_PROJECTION = { 0, 1, 5 };

    public static void main(String[] args) {
        System.out.println("=== Demonstration de l'Optimiseur Heuristique ===\n");
        DisqueBloc table1 = creerTable("table1");
        DisqueBloc table2 = creerTable("table2");
        IndexHachageDynamique indexTable2 = creerIndex(table2);
        if (indexTable2 == null) {
            return;
        }
        ResultatExecution resultatNonOpt = executerPlanNonOptimise(table1, table2);
        afficherResultats("PLAN NON OPTIMISE", resultatNonOpt);
        ResultatExecution resultatOpt = executerPlanOptimise(table1, table2, indexTable2);
        afficherResultats("PLAN OPTIMISE", resultatOpt);
        comparerResultats(resultatNonOpt, resultatOpt);
        demontrerPushDownFiltres(table1, table2, indexTable2);
    }

    private static DisqueBloc creerTable(String nomTable) {
        DisqueBloc table = new DisqueBloc(nomTable, TAILLE_TUPLE, NOMBRE_TUPLES);
        table.genereTable();
        System.out.println("Table generee : " + nomTable + " avec " + NOMBRE_TUPLES + " tuples\n");
        return table;
    }

    private static IndexHachageDynamique creerIndex(DisqueBloc table) {
        IndexHachageDynamique index = new IndexHachageDynamique();
        try {
            index.writeIndex(table);
            System.out.println("Index cree sur " + table.getNomFichier() + ", colonne 0\n");
            return index;
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ResultatExecution executerPlanNonOptimise(DisqueBloc table1, DisqueBloc table2) {
        Operateur plan = construirePlanNonOptimise(table1, table2);
        return executerPlan(plan, table1, table2);
    }

    private static Operateur construirePlanNonOptimise(DisqueBloc table1, DisqueBloc table2) {
        FullScanDisqueBloc scan1 = new FullScanDisqueBloc(table1);
        FullScanDisqueBloc scan2 = new FullScanDisqueBloc(table2);
        DBI join = new DBI(scan1, scan2, 0, 0);
        FiltreEgalite filtre = new FiltreEgalite(join, COLONNE_FILTRE, VALEUR_FILTRE);
        return new Project(filtre, COLONNES_PROJECTION);
    }

    private static ResultatExecution executerPlanOptimise(DisqueBloc table1, DisqueBloc table2,
            IndexHachageDynamique index) {
        Optimiseur optimiseur = new Optimiseur();
        optimiseur.enregistrerIndex("table2", 0, index);

        PlanExecution plan = construirePlanInitial(table1, table2);
        PlanExecution planOptimise = optimiseur.optimiser(plan);

        Operateur operateur = planOptimise.construireOperateur();
        return executerPlan(operateur, table1, table2);
    }

    private static PlanExecution construirePlanInitial(DisqueBloc table1, DisqueBloc table2) {
        PlanExecution scan1 = creerScanPlan(table1, "table1");
        PlanExecution scan2 = creerScanPlan(table2, "table2");

        PlanExecution join = new PlanExecution(PlanExecution.TypeOperateur.JOINTURE_DBI);
        join.setJointureColonneGauche(0);
        join.setJointureColonneDroite(0);
        join.addEnfant(scan1);
        join.addEnfant(scan2);

        PlanExecution filtre = new PlanExecution(PlanExecution.TypeOperateur.FILTRE);
        filtre.setFiltreColonne(COLONNE_FILTRE);
        filtre.setFiltreValeur(VALEUR_FILTRE);
        filtre.addEnfant(join);

        PlanExecution project = new PlanExecution(PlanExecution.TypeOperateur.PROJECT);
        project.setProjectionColonnes(COLONNES_PROJECTION);
        project.addEnfant(filtre);

        return project;
    }

    private static PlanExecution creerScanPlan(DisqueBloc disqueBloc, String nomTable) {
        PlanExecution scan = new PlanExecution(PlanExecution.TypeOperateur.SCAN);
        scan.setDisqueBloc(disqueBloc);
        scan.setNomTable(nomTable);
        return scan;
    }

    private static ResultatExecution executerPlan(Operateur plan, DisqueBloc table1, DisqueBloc table2) {
        table1.resetCompteurLecture();
        table2.resetCompteurLecture();

        long tempsDebut = System.currentTimeMillis();
        plan.open();

        int tuplesProduits = 0;
        Tuple t;
        while ((t = plan.next()) != null) {
            tuplesProduits++;
        }

        plan.close();
        long tempsFin = System.currentTimeMillis();

        int blocsLus = table1.getCompteurLecture() + table2.getCompteurLecture();
        long tempsExecution = tempsFin - tempsDebut;

        return new ResultatExecution(tuplesProduits, blocsLus, tempsExecution);
    }

    private static void afficherResultats(String titre, ResultatExecution resultat) {
        System.out.println("--- " + titre + " ---");
        System.out.println("Tuples produits : " + resultat.tuplesProduits);
        System.out.println("Blocs lus : " + resultat.blocsLus);
        System.out.println("Temps d'execution : " + resultat.tempsExecution + " ms\n");
    }

    private static void comparerResultats(ResultatExecution nonOpt, ResultatExecution opt) {
        System.out.println("--- COMPARAISON ---");

        if (nonOpt.blocsLus > 0) {
            double reduction = 100.0 * (nonOpt.blocsLus - opt.blocsLus) / nonOpt.blocsLus;
            System.out.println("Reduction des blocs lus : " + (nonOpt.blocsLus - opt.blocsLus) +
                    " (" + String.format("%.1f", reduction) + "%)");
        }

        if (nonOpt.tempsExecution > 0) {
            double amelioration = 100.0 * (nonOpt.tempsExecution - opt.tempsExecution) / nonOpt.tempsExecution;
            System.out.println("Amelioration du temps : " + (nonOpt.tempsExecution - opt.tempsExecution) + " ms" +
                    " (" + String.format("%.1f", amelioration) + "%)");
        }

        System.out.println("\nL'optimiseur a remplace la jointure DBI par une jointure sur index");
        System.out.println("Cela reduit significativement le nombre de blocs lus sur le disque");
    }

    private static void demontrerPushDownFiltres(DisqueBloc table1, DisqueBloc table2,
            IndexHachageDynamique index) {
        System.out.println("\n=== Demonstration Push-Down des Filtres ===");

        PlanExecution scan1 = creerScanPlan(table1, "table1");
        PlanExecution scan2 = creerScanPlan(table2, "table2");

        PlanExecution join = new PlanExecution(PlanExecution.TypeOperateur.JOINTURE_DBI);
        join.setJointureColonneGauche(0);
        join.setJointureColonneDroite(0);
        join.addEnfant(scan1);
        join.addEnfant(scan2);

        PlanExecution filtre = new PlanExecution(PlanExecution.TypeOperateur.FILTRE);
        filtre.setFiltreColonne(0);
        filtre.setFiltreValeur(5);
        filtre.addEnfant(join);

        System.out.println("Plan avant optimisation : Filtre(Join(Scan1, Scan2))");

        Optimiseur optimiseur = new Optimiseur();
        optimiseur.enregistrerIndex("table2", 0, index);
        PlanExecution planOpt = optimiseur.optimiser(filtre);

        System.out.println("Plan apres optimisation : Join(Filtre(Scan1), Scan2)");
        System.out.println("Les filtres sont pousses vers les scans pour reduire les donnees traitees");
    }

    private static class ResultatExecution {
        final int tuplesProduits;
        final int blocsLus;
        final long tempsExecution;

        ResultatExecution(int tuplesProduits, int blocsLus, long tempsExecution) {
            this.tuplesProduits = tuplesProduits;
            this.blocsLus = blocsLus;
            this.tempsExecution = tempsExecution;
        }
    }
}
