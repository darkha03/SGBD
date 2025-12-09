
public class ExempleFiltre {
	
	public static void main(String[] args) {
		int tailleTuple = 3;
		TableMemoire source = TableMemoire.randomize(3,  5,  20);
		
		FullScanTableMemoire r1 = new FullScanTableMemoire(source);
		
		r1.open();
		Tuple temp = null;
		while((temp = r1.next()) != null) {
			System.out.println(temp);
		}
		r1.close();
		System.out.println("******************");
		
		
		
		FullScanTableMemoire opFS = new FullScanTableMemoire(source);
		FiltreEgalite filtre1 = new FiltreEgalite(opFS, 0, 2);
		FiltreEgalite opTop = new FiltreEgalite(filtre1, 1, 2);
		
		
		//Operateur opTop = new FiltreEgalite(op1, 1, 3);
		// Requête 1 : Fullscan de la table
		
		
		opTop.open();
		temp = null;
		while((temp = opTop.next()) != null) {
			System.out.println(temp);
		}
		opTop.close();
		
		System.out.println("******************");
		
	}

}
