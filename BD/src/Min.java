
public class Min extends Instrumentation implements Operateur {

	private int col;
	private Operateur in;
	private Tuple tempValMin;
	
	public Min(Operateur _in, int _col) {
		super("Min"+Instrumentation.number++);
		this.start();
		this.col = _col;
		this.in = _in;
		this.tempValMin = null;
		this.stop();
	}
	
	@Override
	public void open() {
		this.start();
		this.in.open();
		this.tuplesProduits = 0;
		this.memoire = 0;
		Tuple temp = null;
		this.tempValMin = this.in.next();
		while((temp = this.in.next())!=null) {
			if(temp.val[this.col] < this.tempValMin.val[this.col]) {
				this.tempValMin = temp;
			}
		}
		this.stop();
	}

	@Override
	public Tuple next() {
		this.start();
		if(this.tempValMin == null) {
			this.stop();
			return null;
		}else {
			Tuple ret = new Tuple(1);
			ret.val[0] = this.tempValMin.val[this.col];
			this.tempValMin = null;
			this.produit(ret);
			this.stop();
			return ret;
		}
	}

	@Override
	public void close() {
		this.in.close();
	}

}
