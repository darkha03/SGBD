
public class Exemple2 {
	
	public static void main(String[] args) {
		
		// Création du contenu de la table
		
		TableMemoire tm = TableMemoire.randomize(5, 100, 100);
		FullScanTableMemoire t = new FullScanTableMemoire(tm);
		
		
		
		// note : t est aussi un operateur FS sur la table créée
		
		// SELECT MIN([2]) FROM T
		
		Min m = new Min(t, 3);
		
		// Exécution de requête
		m.open();
		Tuple temp = null;
		while((temp = m.next())!=null) {
			// Partie applicative : on traite les résultats de la requête 1 par 1
			System.out.println(temp);
		}
		m.close();
		
		
		// Requête exemple

		// Création de données
		
		tm = TableMemoire.randomize(5, 100, 2000);
		FullScanTableMemoire t1 = new FullScanTableMemoire(tm);
		tm = TableMemoire.randomize(4, 100, 2000);

		FullScanTableMemoire t2 = new FullScanTableMemoire(tm);
		
		// /!\ t1 et t2 sont des opérateurs FS sur t1 et t2
		
		// Affichage des tables
	/*	
		System.out.println(" t1 ");
		t1.open();
		temp = null;
		while((temp = t1.next())!=null) {
			// Partie applicative : on traite les résultats de la requête 1 par 1
//			System.out.println(temp);
		}
		t1.close();
		System.out.println("*****************");

		
		System.out.println(" t2 ");
		t2.open();
		temp = null;
		while((temp = t2.next())!=null) {
			// Partie applicative : on traite les résultats de la requête 1 par 1
//			System.out.println(temp);
		}
		t2.close();
		System.out.println("*****************");
*/
		// création d'arbre d'exécution
		
		
		DBI join1 = new DBI(t1, t2, 0, 0);
		FiltreEgalite fe1 = new FiltreEgalite (join1, 2, 3);
		FiltreEgalite fe2 = new FiltreEgalite (fe1, 6, 2);
		int[] att = new int[3];
		att[0] = 0;
		att[1] = 1;
		att[2] = 7;
		Project p = new Project(fe2, att);
		

		
		// Exemple arbre optimisé
		FiltreEgalite fe1p = new FiltreEgalite (t1, 2, 3);
		FiltreEgalite fe2p = new FiltreEgalite (t2, 1, 2);
		DBI join1p = new DBI(fe1p, fe2p, 0, 0);
		int[] attp = new int[3];
		attp[0] = 0;
		attp[1] = 1;
		attp[2] = 7;
		Project pp = new Project(join1p, attp);
		
		
		
		// Execution
		
		
		p.open();
		temp = null;
		while((temp = p.next())!=null) {
			// Partie applicative : on traite les résultats de la requête 1 par 1
//			System.out.println(temp);
		}
		p.close();
		System.out.println(t1);
		System.out.println(t2);
		System.out.println(join1);
		System.out.println(fe1);
		System.out.println(fe2);
		System.out.println(p);
		System.out.println("********");

		
		t1.reset();
		t2.reset();
		pp.open();
		temp = null;
		while((temp = pp.next())!=null) {
			// Partie applicative : on traite les résultats de la requête 1 par 1
//			System.out.println(temp);
		}
		pp.close();
		System.out.println(t1);
		System.out.println(t2);
		System.out.println(fe1p);
		System.out.println(fe2p);
		System.out.println(join1p);
		System.out.println(pp);
		System.out.println("********");


		
		
	}

}
