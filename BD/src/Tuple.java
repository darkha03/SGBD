
public class Tuple {
	
	int[] val;
	int size=0;

	public Tuple(int s) {
		this.val = new int[s];
		this.size = s;
	}
	
	public String toString() {
		String s="";
		for(int i=0;i<this.size;i++) {
			s+=this.val[i]+"\t";
		}
		return s;
	}
}
