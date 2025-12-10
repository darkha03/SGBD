import java.io.*;
import java.util.List;

public class DisqueBloc {
    private int maxTuples = 4;
    private int nombreTuples = 0;
    private int nombreColonnes;
    private String nomFichier;
    private int comteurLecture = 0;

    public DisqueBloc(String nomFichier, int nombreColonnes, int nombreTuples) {
        this.nomFichier = nomFichier;
        this.nombreColonnes = nombreColonnes;
        this.nombreTuples = nombreTuples;
    }

    public void genereTable() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        int nbBlocs = (int) Math.ceil((double) nombreTuples / maxTuples);
        for (int i = 0; i < nbBlocs; i++) {
            String nomBloc = nomFichier + ".bloc" + (i + 1);
            try (FileWriter writer = new FileWriter("data/" + nomBloc)) {
                int tuplesInThisBloc = Math.min(maxTuples, nombreTuples - i * maxTuples);
                int prochainBloc = (i < nbBlocs - 1) ? i + 1 : 0;
                // Ecriture des entetes
                writer.write(tuplesInThisBloc);
                writer.write(nombreColonnes);
                writer.write(prochainBloc);
                for (int j = 0; j < tuplesInThisBloc; j++) {
                    for (int k = 0; k < nombreColonnes; k++) {
                        int valeur = (int) (Math.random() * 100);
                        writer.write(valeur);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public Tuple lireTuple(String nomFichier, int numeroBloc, int indexTuple) {
        String nomBloc = nomFichier + ".bloc" + (numeroBloc + 1);
        Tuple tuple = null;
        try (FileReader reader = new FileReader("data/" + nomBloc)) {
            int nombreTuplesDansBloc = reader.read();
            int nombreColonnesDansBloc = reader.read();
            int prochainBloc = reader.read();
            if (indexTuple < nombreTuplesDansBloc) {
                tuple = new Tuple(nombreColonnesDansBloc);
                for (int i = 0; i < indexTuple; i++) {
                    for (int j = 0; j < nombreColonnesDansBloc; j++) {
                        reader.read(); // Skip values
                    }
                }
                for (int j = 0; j < nombreColonnesDansBloc; j++) {
                    tuple.val[j] = reader.read();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tuple;
    }

    public List<Tuple> lireBloc(String nomFichier, int numeroBloc) {
        String nomBloc = nomFichier + ".bloc" + (numeroBloc + 1);
        List<Tuple> tuplesList = new java.util.ArrayList<>();
        try (FileReader reader = new FileReader("data/" + nomBloc)) {
            int nombreTuplesDansBloc = reader.read();
            int nombreColonnesDansBloc = reader.read();
            int prochainBloc = reader.read();
            for (int i = 0; i < nombreTuplesDansBloc; i++) {
                Tuple tuple = new Tuple(nombreColonnesDansBloc);
                for (int j = 0; j < nombreColonnesDansBloc; j++) {
                    tuple.val[j] = reader.read();
                }
                tuplesList.add(tuple);
            }
            this.comteurLecture++;
            if (prochainBloc != 0) {
                lireBloc(nomFichier, numeroBloc + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tuplesList;
    }

    public int getComteurLecture() {
        return comteurLecture;
    }

    public String getNomFichier() {
        return nomFichier;
    }
    public int getMaxTuples() {
        return maxTuples;
    }
}