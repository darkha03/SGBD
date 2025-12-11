import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JointureTriFusion implements Operateur {
    private Operateur op1, op2;
    private int col1, col2;
    private List<Tuple> table1;
    private List<Tuple> table2;
    private int ptr1, ptr2;
    // Gestion des blocs de doublons
    private int start1, end1;
    private int start2, end2;
    private int current1, current2;
    private boolean processingBlock = false;

    public JointureTriFusion(Operateur op1, Operateur op2, int col1, int col2) {
        this.op1 = op1;
        this.op2 = op2;
        this.col1 = col1;
        this.col2 = col2;
    }

    @Override
    public void open() {
        // Charger intégralement les données en mémoire (in-memory SMJ)
        table1 = new ArrayList<>();
        table2 = new ArrayList<>();

        op1.open();
        Tuple t;
        while ((t = op1.next()) != null)
            table1.add(t);
        op1.close();

        op2.open();
        while ((t = op2.next()) != null)
            table2.add(t);
        op2.close();
        // Tri des tables
        table1.sort(Comparator.comparingInt(tr -> tr.val[col1]));
        table2.sort(Comparator.comparingInt(tr -> tr.val[col2]));
        // Reset pointeurs
        ptr1 = 0;
        ptr2 = 0;
        processingBlock = false;
    }

    @Override
    public Tuple next() {
        // Si on est en plein traitement d'un bloc (produit cartésien local)
        if (processingBlock) {
            Tuple res = joinTuples(table1.get(current1), table2.get(current2));
            current2++;
            if (current2 >= end2) {
                current2 = start2;
                current1++;
                if (current1 >= end1) {
                    processingBlock = false;
                    ptr1 = end1;
                    ptr2 = end2;
                }
            }
            return res;
        }

        while (ptr1 < table1.size() && ptr2 < table2.size()) {
            int v1 = table1.get(ptr1).val[col1];
            int v2 = table2.get(ptr2).val[col2];

            if (v1 < v2) {
                ptr1++;
            } else if (v1 > v2) {
                ptr2++;
            } else {
                // Match trouvé
                findBlocks(v1);
                current1 = start1;
                current2 = start2;
                processingBlock = true;
                return next();
            }
        }
        return null;
    }

    @Override
    public void close() {
        table1 = null;
        table2 = null;
    }

    // Détecter les blocs de valeurs égales autour de ptr1 et ptr2
    private void findBlocks(int value) {
        start1 = ptr1;
        end1 = ptr1 + 1;
        while (end1 < table1.size() && table1.get(end1).val[col1] == value) {
            end1++;
        }
        start2 = ptr2;
        end2 = ptr2 + 1;
        while (end2 < table2.size() && table2.get(end2).val[col2] == value) {
            end2++;
        }
    }

    // Fusionner deux tuples (concaténation des valeurs)
    private Tuple joinTuples(Tuple t1, Tuple t2) {
        Tuple res = new Tuple(t1.size + t2.size);
        System.arraycopy(t1.val, 0, res.val, 0, t1.size);
        System.arraycopy(t2.val, 0, res.val, t1.size, t2.size);
        return res;
    }
}
