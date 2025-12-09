public class FullScanMemoire implements Operateur {
    private TableMemoire contenu;
	int compteur = 0;
	int taille = 0;
	int range = 5;
	long total;
    int tuplesProduits = 0;
    int memoire = 0;

    public FullScanMemoire(TableMemoire tableMemoire) {
        this.contenu = tableMemoire;
        this.taille = this.contenu.valeurs.size();
		this.total = 0;
    }

    public void open() {
		this.compteur = 0;
		this.tuplesProduits = 0;
		this.memoire = 0;
	}
	
	public Tuple next() {
		if(this.compteur<this.taille) {
			Tuple t = this.contenu.valeurs.elementAt(this.compteur++); 
			this.produit(t);
			return(t);
		}
		else {
			return null;
		}
	}
	
	public void close(){
		this.total+=this.tuplesProduits;
	}

    public void produit(Tuple t) {
		this.tuplesProduits++;
		this.memoire+=t.size;
	}
}