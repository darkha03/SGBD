public class Avg extends Instrumentation implements Operateur  {

    private int col;
    private Operateur in;
    private Tuple tempValAvg;

    public Avg(Operateur _in, int _col) {
        super("Average " + Instrumentation.number++);
        this.start();
        this.col = _col;
        this.in = _in;
        this.tempValAvg = null;
        this.stop();
    }

    @Override
    public void open() {
        this.start();
        this.in.open();
        this.tuplesProduits = 0;
		this.memoire = 0;
		Tuple temp = null;
		double sum = 0;
        int count = 0;
        while((temp = this.in.next())!=null) {
            sum += temp.val[this.col];
            count++;
        }
        this.tempValAvg = new Tuple(1);
        if (count > 0) {
            this.tempValAvg.val[0] = (int)(sum / count);
        } else {
            this.tempValAvg.val[0] = 0;
        }
		this.stop();
    }

    @Override
    public Tuple next() {
        this.start();
        if(this.tempValAvg == null) {
            this.stop();
            return null;
        } else {
            Tuple ret = new Tuple(1);
            ret.val[0] = this.tempValAvg.val[0];
            this.tempValAvg = null;
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