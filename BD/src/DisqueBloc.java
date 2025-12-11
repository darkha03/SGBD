import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class DisqueBloc {
    private int maxTuples = 4;
    private int nombreTuples = 0;
    private int nombreColonnes;
    private String nomFichier;
    private int compteurLecture = 0;

    public DisqueBloc(String nomFichier, int nombreColonnes, int nombreTuples) {
        this.nomFichier = nomFichier;
        this.nombreColonnes = nombreColonnes;
        this.nombreTuples = nombreTuples;
        this.compteurLecture = 0; // Initialisation explicite
    }

    public void genereTable() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        int nbBlocs = (int) Math.ceil((double) nombreTuples / maxTuples);
        for (int i = 0; i < nbBlocs; i++) {
            String nomBloc = nomFichier + ".bloc" + (i + 1);
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/" + nomBloc))) {
                int tuplesInThisBloc = Math.min(maxTuples, nombreTuples - i * maxTuples);
                int prochainBloc = (i < nbBlocs - 1) ? (i + 2) : 0; // i+2 car les blocs commencent à 1
                // Ecriture des entetes
                dos.writeInt(nombreColonnes);
                dos.writeInt(tuplesInThisBloc);
                dos.writeInt(prochainBloc);
                for (int j = 0; j < tuplesInThisBloc; j++) {
                    for (int k = 0; k < nombreColonnes; k++) {
                        int valeur = (int) (Math.random() * 100);
                        dos.writeInt(valeur);
                    }
                }
                System.out.println("Bloc " + (i + 1) + " créé avec " + tuplesInThisBloc + " tuples");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Table " + nomFichier + " générée avec " + nbBlocs + " blocs");
    }

    public Tuple lireTuple(String nomFichier, int numeroBloc, int indexTuple) {
        String nomBloc = nomFichier + ".bloc" + numeroBloc;
        Tuple tuple = null;
        try (DataInputStream dis = new DataInputStream(new FileInputStream("data/" + nomBloc))) {
            int nombreColonnesDansBloc = dis.readInt();
            int nombreTuplesDansBloc = dis.readInt();
            int prochainBloc = dis.readInt();
            if (indexTuple < nombreTuplesDansBloc) {
                tuple = new Tuple(nombreColonnesDansBloc);
                for (int i = 0; i < indexTuple; i++) {
                    for (int j = 0; j < nombreColonnesDansBloc; j++) {
                        dis.readInt(); // Skip values
                    }
                }
                for (int j = 0; j < nombreColonnesDansBloc; j++) {
                    tuple.val[j] = dis.readInt();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tuple;
    }

    public List<Tuple> lireBloc(String nomFichier, int numeroBloc) {
        String nomBloc = nomFichier + ".bloc" + numeroBloc;
        List<Tuple> tuplesList = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream("data/" + nomBloc))) {
            int nombreColonnesDansBloc = dis.readInt();
            int nombreTuplesDansBloc = dis.readInt();
            int prochainBloc = dis.readInt();
            for (int i = 0; i < nombreTuplesDansBloc; i++) {
                Tuple tuple = new Tuple(nombreColonnesDansBloc);
                for (int j = 0; j < nombreColonnesDansBloc; j++) {
                    tuple.val[j] = dis.readInt();
                }
                tuplesList.add(tuple);
            }
            this.compteurLecture++;
            if (prochainBloc != 0) {
                tuplesList.addAll(lireBloc(nomFichier, prochainBloc));
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture bloc " + numeroBloc + ": " + e.getMessage());
        }
        return tuplesList;
    }

    public int getCompteurLecture() {
        return compteurLecture;
    }

    public void incrementerCompteurLecture() {
        this.compteurLecture++;
    }

    public void resetCompteurLecture() {
        this.compteurLecture = 0;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public int getMaxTuples() {
        return maxTuples;
    }

    public int getNombreColonnes() {
        return nombreColonnes;
    }
}