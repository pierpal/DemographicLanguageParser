package demographicLanguageParser;

import java.util.ArrayList;

/**
 * This class holds a period of time, with all the information required to compute
 * coalescent probabilities within it.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class TimeFrame {

    static final int MAX_TIME = 10000;
    Generation Gstart, Gend;
    MigrationMatrix migration;
    ArrayList<MigrationMatrix> transitions;
    int intStart, intEnd;

    public TimeFrame(Generation Gstart, Generation Gend,
            MigrationMatrix migration) {
        this.Gstart = Gstart;
        this.Gend = Gend;
        this.migration = migration;
        this.intStart = (int) Math.round(Gstart.getValue());
        this.intEnd = (Gend.getValue() == Double.POSITIVE_INFINITY) ? TimeFrame.MAX_TIME : (int) Math.round(Gend.getValue());
    }

    public void addTransition(MigrationMatrix transition) {
        if (this.transitions == null) {
            this.transitions = new ArrayList<MigrationMatrix>();
        }
        this.transitions.add(transition);
    }

    public Matrix[] computeCoalescenceVector(Matrix initialState) throws Exception {
        if (DemographicLanguageParser.debugIsOn()) {
            System.out.println("Start of " + intStart + " " + intEnd);
        }
        Matrix[] coalescence = new Matrix[intEnd - intStart];
        Edge[] populations = migration.populationsFrom;
        Double[] sizes = new Double[populations.length];
        for (int g = 1; g <= (intEnd - intStart) - 1; g++) {
            initialState.multiplyMatrices(migration.getMigMatrix());
            for (int i = 0; i < populations.length; i++) {
                sizes[i] = populations[i].getSizeAt(intStart + g);
                if (DemographicLanguageParser.debugIsOn()) {
                    System.out.println("gen " + (intStart + g) + " pop " + populations[i].getId() + " size " + sizes[i]);
                }
            }
            coalescence[g - 1] = Matrix.multiplyDiagSelf(initialState, sizes);
        }
        initialState.multiplyMatrices(migration.getMigMatrix());
        for (int i = 0; i < populations.length; i++) {
            sizes[i] = populations[i].getSizeAt(Gend.getValue());
        }
        if (transitions != null) {
            for (int i = 0; i < transitions.size(); i++) {
                initialState.multiplyMatrices(transitions.get(i).migMat);
            }
            populations = transitions.get(transitions.size() - 1).populationsTo;
            sizes = new Double[populations.length];
            for (int i = 0; i < populations.length; i++) {
                sizes[i] = populations[i].getSizeAt(intEnd);
                if (DemographicLanguageParser.debugIsOn()) {
                    System.out.println("gen " + (intEnd) + " pop " + populations[i].getId() + " size " + sizes[i]);
                }
            }
        }
        coalescence[(intEnd - intStart) - 1] = Matrix.multiplyDiagSelf(initialState, sizes);
        return coalescence;
    }
}
