
public class Exemple1 {

	public static void main(String[] args) {

		// Création de la table + création de l'opérateur FullScan
		TableMemoire tm = TableMemoire.randomize(5, 5, 10);
		FullScanTableMemoire t1 = new FullScanTableMemoire(tm);
		//
		
		t1.open();
		Tuple temp = null;
		while((temp = t1.next()) != null) {
			System.out.println(temp);
		}
		t1.close();
		
		System.out.println("******************");
		
		//  Crée un arbre d'exécution
		// FE <--- FS
		
		FiltreEgalite f1 = new FiltreEgalite(t1, 0, 2);
		
		f1.open();
		while((temp = f1.next()) != null) {
			System.out.println(temp);
		}
		f1.close();

		System.out.println("******************");


		// Min <--- FE <--- FS
		
		Min m1 = new Min(t1, 2);
		m1.open();
		while((temp = m1.next()) != null) {
			System.out.println(temp);
		}
		m1.close();

		System.out.println("******************");

		
		int[] atts = new int[2];
		atts[0]=0;
		atts[1]=2;
		Project p1 = new Project(t1, atts);

		p1.open();
		while((temp = p1.next()) != null) {
			System.out.println(temp);
		}
		p1.close();

		
	}

}
