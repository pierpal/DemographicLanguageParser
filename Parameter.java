package demographicLanguageParser;

/**
 * Abstract class for a generic parameter.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public abstract class Parameter implements Comparable<Parameter> {

    // for numerical precision, when equalities are required, use at most epsilon difference.
    private static final double epsilon = 1E-10;

    /**
     * @return the epsilon
     */
    public static double getEpsilon() {
        return epsilon;
    }
    // the value of the parameter
    private Double value;
    // the number of grid points in the initialization
    private int gridPoints;
    // true if grid points = 0
    boolean isConstant;
    // name of the parameter
    private String id;

    /**
     * Prints parameter
     */
    @Override
    public String toString() {
        return this.getId() + "_" + this.getValue() + "_" + this.getGridPoints();
    }

    /**
     * Return value
     * @return value of parameter
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * Empty constructor, builds empty parameter
     */
    public Parameter() {
    }

    /**
     * Builds a parameter
     * @param value The parameter value
     * @param gridPoints Number of grid points
     */
    public Parameter(double value, int gridPoints) {
        this.value = value;
        this.gridPoints = gridPoints;
        this.isConstant = (this.gridPoints == 0) ? true : false;
    }

    /**
     * Set the parameter name
     * @param id The name
     */
    public void setName(String id) {
        this.setId(id);
    }

    /**
     * comparison is on value, if same, use name if specified, otherwise return any.
     * NOTE: if no name and same value, sets may keep one of two identical objects
     * @param p2 The other parameter
     * @return 1 if the value of p2 is smaller, -1 if larger. If value is same compare names. 0 if same value and no name set.
     */
    public int compareTo(Parameter p2) {
        if (p2.getValue() < this.getValue()) {
            return 1;
        } else if (p2.getValue() > this.getValue()) {
            return -1;
        } else {
            if (getId() != null && p2.getId() != null) {
                return getId().compareTo(p2.getId());
            } else {
                // name not set yet, return same
                return 0;
            }
        }
    }

    /**
     * Try to update a parameter
     * @return 0 if parameter updated, 1 if range violated, 2 if conservation violated, 3 if migration violated
     */
    public abstract int tryUpdate(double increment);

    /**
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * @return the gridPoints
     */
    public int getGridPoints() {
        return gridPoints;
    }

    /**
     * @param gridPoints the gridPoints to set
     */
    public void setGridPoints(int gridPoints) {
        this.gridPoints = gridPoints;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
