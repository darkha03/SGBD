import java.io.*;

public class FullScanDisqueBloc implements Operateur {
    private DisqueBloc disqueBloc;
    private int compteur = 0;
    private int taille = 0;
    private String nomFichier;
    private int currentBloc;
    private int prochainBloc = 0;
    private int nombreColonnes = 0;
    private int blocsLus = 0;

    public FullScanDisqueBloc(DisqueBloc disqueBloc) {
        this(disqueBloc, 1);
    }

    public FullScanDisqueBloc(DisqueBloc disqueBloc, int startBloc) {
        this.disqueBloc = disqueBloc;
        this.nomFichier = disqueBloc.getNomFichier();
        this.currentBloc = startBloc;
    }

    @Override
    public void open() {
        compteur = 0;
        blocsLus = 0;
        chargerBlocCourant();
    }

    private void chargerBlocCourant() {
        String nomBlocFichier = this.nomFichier + ".bloc" + currentBloc;
        try (DataInputStream dis = new DataInputStream(new FileInputStream("data/" + nomBlocFichier))) {
            nombreColonnes = dis.readInt();
            taille = dis.readInt();
            prochainBloc = dis.readInt();
            blocsLus++;
            disqueBloc.incrementerCompteurLecture();

        } catch (IOException e) {
            System.err.println("Info: Could not open block " + currentBloc + " (End of chain or file missing)");
            taille = 0;
            prochainBloc = 0;
        }
    }

    public int getBlocsLus() {
        return blocsLus;
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
                compteur = 0;
                chargerBlocCourant();
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