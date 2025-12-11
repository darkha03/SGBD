import java.io.File;
import java.util.List;

public class ExempleJointureTriFusion {
    public static void main(String[] args) {
        DisqueBloc tableRDisk = new DisqueBloc("tableR", 2, 10);
        DisqueBloc tableSDisk = new DisqueBloc("tableS", 3, 10);
        tableRDisk.genereTable();
        tableSDisk.genereTable();
        // Affiche 2 Table
        System.out.println("TABLE R : ");
        Operateur scanR_display = new FullScanDisqueBloc(tableRDisk);
        scanR_display.open();
        Tuple t;
        while ((t = scanR_display.next()) != null) {
            System.out.println(t);
        }
        scanR_display.close();
        System.out.println("TABLE S : ");
        Operateur scanS_display = new FullScanDisqueBloc(tableSDisk);
        scanS_display.open();
        while ((t = scanS_display.next()) != null) {
            System.out.println(t);
        }
        scanS_display.close();
        // Jointure
        Operateur scanR = new FullScanDisqueBloc(tableRDisk);
        Operateur scanS = new FullScanDisqueBloc(tableSDisk);
        JointureTriFusion join = new JointureTriFusion(scanR, scanS, 0, 0);
        join.open();
        System.out.println("RÉSULTAT DE LA JOINTURE");
        int count = 0;
        while ((t = join.next()) != null) {
            System.out.println(t);
            count++;
        }
        if (count == 0) {
            System.out.println("(Aucun tuple ne correspond)");
        }
        join.close();
    }
}
