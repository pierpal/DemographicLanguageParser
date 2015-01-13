package demographicLanguageParser;

/**
 * Size parameter.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Size extends Parameter {

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
    private Double max = Double.POSITIVE_INFINITY;
    private static int created = 0;

    /**
     * Constructor
     * @param value value of rate
     * @param gridPoints number of grid points
     */
    public Size(double value, int gridPoints) {
        super(value, gridPoints);
        created++;
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

}
