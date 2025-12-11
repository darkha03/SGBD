import java.io.*;

public class FullScanDisqueBloc implements Operateur {
    private DisqueBloc disqueBloc;
    private int compteur = 0;
    private int taille = 0;
    private String nomFichier;
    private int currentBloc = 0;
    private int prochainBloc = 0;
    private boolean fichierScan = true;

    public FullScanDisqueBloc(DisqueBloc disqueBloc) {
        this(disqueBloc, 0);
        this.fichierScan = true;
    }

    public FullScanDisqueBloc(DisqueBloc disqueBloc, int currentBloc) {
        this.disqueBloc = disqueBloc;
        this.nomFichier = disqueBloc.getNomFichier();
        this.currentBloc = currentBloc;
        this.fichierScan = false;
    }

    @Override
    public void open() {
        compteur = 0;
        
        String nomFichierComplet = fichierScan ? this.nomFichier + ".bloc" + (currentBloc + 1) : this.nomFichier;
        try (FileReader reader = new FileReader("data/" + nomFichierComplet)) {
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
            // Passer nomFichier sans le numéro de bloc à lireTuple
            String baseNomFichier = fichierScan ? this.nomFichier : this.nomFichier.substring(0, this.nomFichier.lastIndexOf(".bloc"));
            Tuple t = disqueBloc.lireTuple(baseNomFichier, currentBloc, compteur);
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
