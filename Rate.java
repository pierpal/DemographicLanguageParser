package demographicLanguageParser;

import java.util.ArrayList;

/**
 * Rate parameter.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Rate extends Parameter {

    /**
     * @return the R0
     */
    public static Rate getR0() {
        return R0;
    }

    /**
     * @param aR0 the R0 to set
     */
    public static void setR0(Rate aR0) {
        R0 = aR0;
    }

    /**
     * @return the R1
     */
    public static Rate getR1() {
        return R1;
    }

    /**
     * @param aR1 the R1 to set
     */
    public static void setR1(Rate aR1) {
        R1 = aR1;
    }

    /**
     * @return the created
     */
    public static int getCreated() {
        return created;
    }

    /**
     * @param aCreated the created to set
     */
    public static void setCreated(int aCreated) {
        created = aCreated;
    }
    // counter of created objects
    private Double min = 0.0;
    private Double max = 1.0;
    private static Rate R0;
    private static Rate R1;
    private static int created = 0;
    // matrices this rate appears in
    private ArrayList<MigrationMatrix> matricesAppearsIn = new ArrayList<MigrationMatrix>();

    /**
     * Constructor
     * @param value value of rate
     * @param gridPoints number of grid points
     */
    public Rate(double value, int gridPoints) {
        super(value, gridPoints);
        created++;
    }
    
    /**
     * add a migration matrix this rate appears in, to keep track of constraints
     * @param m the migration matrix
     */
    public void addMatrix(MigrationMatrix m) {
        getMatricesAppearsIn().add(m);
    }

    /**
     * Try to update a parameter
     * @return 0 if parameter updated, 1 if range violated, 2 if conservation violated, 3 if migration violated
     */
    public int tryUpdate(double increment) {
        return 0;
    }

    /**
     * @return the min
     */
    public Double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public Double getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * @return the matricesAppearsIn
     */
    public ArrayList<MigrationMatrix> getMatricesAppearsIn() {
        return matricesAppearsIn;
    }

    /**
     * @param matricesAppearsIn the matricesAppearsIn to set
     */
    public void setMatricesAppearsIn(ArrayList<MigrationMatrix> matricesAppearsIn) {
        this.matricesAppearsIn = matricesAppearsIn;
    }

}
