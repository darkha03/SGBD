
public class FiltreEgalite extends Instrumentation implements Operateur {
	
	private Operateur in;
	private int col;
	private int val;
	
	// Operateur qui produit les tuples v�rifiant attribut _col = _val
	public FiltreEgalite(Operateur _in, int _col, int _val){
		super("FiltreEgalite"+Instrumentation.number++);
		this.in = _in;
		this.col = _col;
		this.val = _val;
	}

	@Override
	public void open() {
		this.start();
		this.in.open();
		this.tuplesProduits = 0;
		this.memoire = 0;
		this.stop();
	}

	@Override
	public Tuple next() {
		this.start();
		Tuple t = null;
		while((t=(this.in.next()))!=null){
			if(t.val[this.col]==this.val){
				this.produit(t);
				this.stop();
				return t;
			}
			else{
				this.stop();
				return this.next();
			}
		}
		this.stop();
		return t;
	}

	@Override
	public void close() {
		this.in.close();		
	}

}
