import java.util.*;

public class Optimiseur {
    private final Map<String, Map<Integer, IndexHachageDynamique>> indexDisponibles;

    public Optimiseur() {
        this.indexDisponibles = new HashMap<>();
    }

    public void enregistrerIndex(String nomTable, int colonne, IndexHachageDynamique index) {
        indexDisponibles.computeIfAbsent(nomTable, k -> new HashMap<>()).put(colonne, index);
    }

    public PlanExecution optimiser(PlanExecution plan) {
        PlanExecution planOptimise = plan.cloner();
        // L'ordre d'application des règles
        planOptimise = remplacerParIndexJoin(planOptimise);
        planOptimise = pushDownFiltres(planOptimise);
        planOptimise = pushDownProjections(planOptimise);
        return planOptimise;
    }

    private PlanExecution pushDownFiltres(PlanExecution plan) {
        PlanExecution nouveauPlan = plan.cloner();
        if (estFiltreAvecEnfant(nouveauPlan)) {
            PlanExecution enfant = nouveauPlan.getEnfants().get(0);
            if (enfant.estJointure()) {
                return pousserFiltreSurJointure(nouveauPlan, enfant);
            } else {
                nouveauPlan.getEnfants().set(0, pushDownFiltres(enfant));
                return nouveauPlan;
            }
        }
        appliquerRecursivement(nouveauPlan, this::pushDownFiltres);
        return nouveauPlan;
    }

    private boolean estFiltreAvecEnfant(PlanExecution plan) {
        return plan.getType() == PlanExecution.TypeOperateur.FILTRE && !plan.getEnfants().isEmpty();
    }

    private PlanExecution pousserFiltreSurJointure(PlanExecution filtre, PlanExecution jointure) {
        PlanExecution filtreGauche = creerFiltre(filtre, pushDownFiltres(jointure.getEnfants().get(0)));
        PlanExecution nouvelleJointure = jointure.cloner();
        nouvelleJointure.getEnfants().clear();
        nouvelleJointure.addEnfant(filtreGauche);
        if (jointure.getEnfants().size() > 1) {
            nouvelleJointure.addEnfant(pushDownFiltres(jointure.getEnfants().get(1)));
        }

        return nouvelleJointure;
    }

    private PlanExecution creerFiltre(PlanExecution filtreSource, PlanExecution enfant) {
        PlanExecution nouveauFiltre = new PlanExecution(PlanExecution.TypeOperateur.FILTRE);
        nouveauFiltre.setFiltreColonne(filtreSource.getFiltreColonne());
        nouveauFiltre.setFiltreValeur(filtreSource.getFiltreValeur());
        nouveauFiltre.addEnfant(enfant);
        return nouveauFiltre;
    }

    private void appliquerRecursivement(PlanExecution plan,
            java.util.function.Function<PlanExecution, PlanExecution> transformation) {
        for (int i = 0; i < plan.getEnfants().size(); i++) {
            plan.getEnfants().set(i, transformation.apply(plan.getEnfants().get(i)));
        }
    }

    private PlanExecution pushDownProjections(PlanExecution plan) {
        PlanExecution nouveauPlan = plan.cloner();
        if (nouveauPlan.getType() == PlanExecution.TypeOperateur.PROJECT && !nouveauPlan.getEnfants().isEmpty()) {
            PlanExecution enfant = nouveauPlan.getEnfants().get(0);
            nouveauPlan.getEnfants().set(0, pushDownProjections(enfant));
            return nouveauPlan;
        }
        appliquerRecursivement(nouveauPlan, this::pushDownProjections);
        return nouveauPlan;
    }

    private PlanExecution remplacerParIndexJoin(PlanExecution plan) {
        PlanExecution nouveauPlan = plan.cloner();
        if (nouveauPlan.getType() == PlanExecution.TypeOperateur.JOINTURE_DBI && nouveauPlan.getEnfants().size() >= 2) {
            IndexHachageDynamique index = trouverIndexPourJointure(nouveauPlan);
            if (index != null) {
                nouveauPlan.setType(PlanExecution.TypeOperateur.JOINTURE_INDEX);
                nouveauPlan.setIndexHachage(index);
                return nouveauPlan;
            }
        }
        appliquerRecursivement(nouveauPlan, this::remplacerParIndexJoin);
        return nouveauPlan;
    }

    private IndexHachageDynamique trouverIndexPourJointure(PlanExecution jointure) {
        PlanExecution scanDroit = trouverScan(jointure.getEnfants().get(1));
        if (scanDroit == null || scanDroit.getNomTable() == null) {
            return null;
        }
        Map<Integer, IndexHachageDynamique> indexTable = indexDisponibles.get(scanDroit.getNomTable());
        if (indexTable == null) {
            return null;
        }
        return indexTable.get(jointure.getJointureColonneDroite());
    }

    private PlanExecution trouverScan(PlanExecution plan) {
        if (plan.getType() == PlanExecution.TypeOperateur.SCAN) {
            return plan;
        }
        for (PlanExecution enfant : plan.getEnfants()) {
            PlanExecution scan = trouverScan(enfant);
            if (scan != null) {
                return scan;
            }
        }
        return null;
    }
}
