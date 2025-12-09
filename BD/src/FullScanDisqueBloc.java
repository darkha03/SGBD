import java.io.*;

public class FullScanDisqueBloc implements Operateur {
    private DisqueBloc disqueBloc;
    private int compteur = 0;
    private int taille = 0;
    private String nomFichier;
    private int currentBloc = 0;
    private int prochainBloc = 0;

    public FullScanDisqueBloc(DisqueBloc disqueBloc) {
        this.disqueBloc = disqueBloc;
        this.nomFichier = disqueBloc.getNomFichier();
    }

    @Override
    public void open() {
        compteur = 0;
        String nomFichier = this.nomFichier + ".bloc" + (currentBloc + 1);
        try (FileReader reader = new FileReader("data/" + nomFichier)) {
            int nombreTuplesDansBloc = reader.read();
            taille = nombreTuplesDansBloc;
            int nombreColonnesDansBloc = reader.read();
            prochainBloc = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple next() {
        if (compteur < taille) {
            Tuple t = disqueBloc.lireTuple(nomFichier, currentBloc, compteur);
            compteur++;
            return t;
        } else {
            if (prochainBloc != 0) {
                currentBloc = prochainBloc;
                open();
                return next();
            } else {
                return null;
            }
        }
    }
    
    @Override
    public void close() {
    }
}
