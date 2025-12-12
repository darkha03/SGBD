import java.util.*;

public class PlanExecution {
    public enum TypeOperateur {
        SCAN,
        FILTRE,
        PROJECT,
        JOINTURE_DBI,
        JOINTURE_INDEX,
        JOINTURE_TRI,
        AUTRE
    }

    private TypeOperateur type;
    private Operateur operateur;
    private int filtreColonne;
    private int filtreValeur;
    private int[] projectionColonnes;
    private int jointureColonneGauche;
    private int jointureColonneDroite;
    private IndexHachageDynamique indexHachage;
    private DisqueBloc disqueBloc;
    private List<PlanExecution> enfants;
    private String nomTable;

    public PlanExecution(TypeOperateur type) {
        this.type = type;
        this.enfants = new ArrayList<>();
    }

    // Getters et setters
    public TypeOperateur getType() {
        return type;
    }

    public void setType(TypeOperateur type) {
        this.type = type;
    }

    public Operateur getOperateur() {
        return operateur;
    }

    public void setOperateur(Operateur operateur) {
        this.operateur = operateur;
    }

    public int getFiltreColonne() {
        return filtreColonne;
    }

    public void setFiltreColonne(int col) {
        this.filtreColonne = col;
    }

    public int getFiltreValeur() {
        return filtreValeur;
    }

    public void setFiltreValeur(int val) {
        this.filtreValeur = val;
    }

    public int[] getProjectionColonnes() {
        return projectionColonnes;
    }

    public void setProjectionColonnes(int[] cols) {
        this.projectionColonnes = cols;
    }

    public int getJointureColonneGauche() {
        return jointureColonneGauche;
    }

    public void setJointureColonneGauche(int col) {
        this.jointureColonneGauche = col;
    }

    public int getJointureColonneDroite() {
        return jointureColonneDroite;
    }

    public void setJointureColonneDroite(int col) {
        this.jointureColonneDroite = col;
    }

    public IndexHachageDynamique getIndexHachage() {
        return indexHachage;
    }

    public void setIndexHachage(IndexHachageDynamique index) {
        this.indexHachage = index;
    }

    public DisqueBloc getDisqueBloc() {
        return disqueBloc;
    }

    public void setDisqueBloc(DisqueBloc db) {
        this.disqueBloc = db;
    }

    public List<PlanExecution> getEnfants() {
        return enfants;
    }

    public void addEnfant(PlanExecution enfant) {
        this.enfants.add(enfant);
    }

    public String getNomTable() {
        return nomTable;
    }

    public void setNomTable(String nom) {
        this.nomTable = nom;
    }

    public Operateur construireOperateur() {
        if (operateur != null) {
            return operateur;
        }

        List<Operateur> enfantsOperateurs = construireEnfants();
        operateur = creerOperateur(enfantsOperateurs);

        return operateur;
    }

    private List<Operateur> construireEnfants() {
        List<Operateur> enfantsOperateurs = new ArrayList<>();
        for (PlanExecution enfant : enfants) {
            enfantsOperateurs.add(enfant.construireOperateur());
        }
        return enfantsOperateurs;
    }

    private Operateur creerOperateur(List<Operateur> enfantsOperateurs) {
        switch (type) {
            case SCAN:
                return creerScan();

            case FILTRE:
                return creerFiltre(enfantsOperateurs);

            case PROJECT:
                return creerProject(enfantsOperateurs);

            case JOINTURE_DBI:
                return creerJointureDBI(enfantsOperateurs);

            case JOINTURE_INDEX:
                return creerJointureIndex(enfantsOperateurs);

            case JOINTURE_TRI:
                return creerJointureTri(enfantsOperateurs);

            default:
                return null;
        }
    }

    private Operateur creerScan() {
        if (disqueBloc != null) {
            return new FullScanDisqueBloc(disqueBloc);
        }
        return null;
    }

    private Operateur creerFiltre(List<Operateur> enfantsOperateurs) {
        if (!enfantsOperateurs.isEmpty()) {
            return new FiltreEgalite(enfantsOperateurs.get(0), filtreColonne, filtreValeur);
        }
        return null;
    }

    private Operateur creerProject(List<Operateur> enfantsOperateurs) {
        if (!enfantsOperateurs.isEmpty() && projectionColonnes != null) {
            return new Project(enfantsOperateurs.get(0), projectionColonnes);
        }
        return null;
    }

    private Operateur creerJointureDBI(List<Operateur> enfantsOperateurs) {
        if (enfantsOperateurs.size() >= 2) {
            return new DBI(enfantsOperateurs.get(0), enfantsOperateurs.get(1),
                    jointureColonneGauche, jointureColonneDroite);
        }
        return null;
    }

    private Operateur creerJointureIndex(List<Operateur> enfantsOperateurs) {
        if (enfantsOperateurs.size() >= 2 && indexHachage != null) {
            return new JointureSurIndex(enfantsOperateurs.get(0), enfantsOperateurs.get(1),
                    indexHachage, jointureColonneGauche, jointureColonneDroite);
        }
        return null;
    }

    private Operateur creerJointureTri(List<Operateur> enfantsOperateurs) {
        if (enfantsOperateurs.size() >= 2) {
            return new JointureTriFusion(enfantsOperateurs.get(0), enfantsOperateurs.get(1),
                    jointureColonneGauche, jointureColonneDroite);
        }
        return null;
    }

    public PlanExecution cloner() {
        PlanExecution clone = new PlanExecution(this.type);
        copierAttributs(clone);
        clonerEnfants(clone);
        return clone;
    }

    private void copierAttributs(PlanExecution clone) {
        clone.filtreColonne = this.filtreColonne;
        clone.filtreValeur = this.filtreValeur;
        clone.projectionColonnes = copierTableau(projectionColonnes);
        clone.jointureColonneGauche = this.jointureColonneGauche;
        clone.jointureColonneDroite = this.jointureColonneDroite;
        clone.indexHachage = this.indexHachage;
        clone.disqueBloc = this.disqueBloc;
        clone.nomTable = this.nomTable;
    }

    private void clonerEnfants(PlanExecution clone) {
        for (PlanExecution enfant : this.enfants) {
            clone.addEnfant(enfant.cloner());
        }
    }

    private int[] copierTableau(int[] tableau) {
        return tableau != null ? Arrays.copyOf(tableau, tableau.length) : null;
    }

    public List<PlanExecution> trouverNoeuds(TypeOperateur typeRecherche) {
        List<PlanExecution> resultats = new ArrayList<>();
        if (this.type == typeRecherche) {
            resultats.add(this);
        }
        for (PlanExecution enfant : enfants) {
            resultats.addAll(enfant.trouverNoeuds(typeRecherche));
        }
        return resultats;
    }

    public List<PlanExecution> trouverScans() {
        return trouverNoeuds(TypeOperateur.SCAN);
    }

    public boolean estJointure() {
        return type == TypeOperateur.JOINTURE_DBI
                || type == TypeOperateur.JOINTURE_INDEX
                || type == TypeOperateur.JOINTURE_TRI;
    }
}
