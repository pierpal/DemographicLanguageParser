package demographicLanguageParser;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Generation parameter.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Generation extends Parameter {

    /**
     * @return the G0
     */
    public static Generation getG0() {
        return G0;
    }

    /**
     * @param aG0 the G0 to set
     */
    public static void setG0(Generation aG0) {
        G0 = aG0;
    }

    /**
     * @return the Ginf
     */
    public static Generation getGinf() {
        return Ginf;
    }

    /**
     * @param aGinf the Ginf to set
     */
    public static void setGinf(Generation aGinf) {
        Ginf = aGinf;
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

    // true if this generation is at a constant distance from another generation
    private boolean isOffset = false;
    // offset generation
    private Generation offsetGeneration;
    // distance to offset generation
    private int offset;
    // counter of created generations
    private static Generation G0;
    // nodes that are linked to this generation
    private static Generation Ginf;
    // contraints (a set of nodes above and below, defined by edges
    private static int created = 0;
    private HashSet<Node> nodes = new HashSet<Node>();
    // when an offset node is created, its constraints are added here
    private HashSet<Generation> lessThan = new HashSet<Generation>();
    private HashSet<Generation> moreThan = new HashSet<Generation>();
    private HashMap<Generation, Integer> lessThanOffsetNodes = new HashMap<Generation, Integer>();
    private HashMap<Generation, Integer> moreThanOffsetNodes = new HashMap<Generation, Integer>();

    /**
     * Generation constructor
     * @param value the generation value
     * @param gridPoints the number of grid points
     */
    public Generation(double value, int gridPoints) {
        super(value, gridPoints);
        created++;
    }

    /**
     * Return value
     * @return value of parameter
     */
    @Override
    public Double getValue() {
        if (isIsOffset() == false) {
            return super.getValue();
        }
        else {
            return getOffsetGeneration().getValue() + getOffset();
        }
    }

    /**
     * Create a generation at a fixed distance from another generation
     * @param the other generation
     * @param the distance from it
     */
    public Generation(Generation offsetGeneration, int offset) throws Exception {
        if (offset < 1) {
            throw new Exception("Attemped to create a generation parameter with offset smaller than 1");
        }
        this.offsetGeneration = offsetGeneration;
        this.offset = offset;
        this.isOffset = true;
        this.isConstant = offsetGeneration.isConstant;
        created++;
    }

    /**
     * Adds upper bound
     * @param lessThan the bounding generation
     */
    public void addLessThan(Generation lessThan) {
        // allow instantaneous edges
        if (lessThan != this) {
            this.getLessThan().add(lessThan);
        }
    }

    /**
     * Adds lower bound
     * @param moreThan the bounding generation
     */
    public void addMoreThan(Generation moreThan) {
        // allow instantaneous edges
        if (moreThan != this) {
            this.getMoreThan().add(moreThan);
        }
    }

    /**
     * Returns min of upper bounds
     * @return min of upper bounds
     */
    public double getAtMost() {
        double min = Double.POSITIVE_INFINITY;
        for (Generation g : getLessThan()) {
            if (g.getValue() < min) {
                min = g.getValue();
            }
        }
        return min;
    }

    /**
     * Returns max of lower bounds
     * @return max of lower bounds
     */
    public double getAtLeast() {
        double max = Double.NEGATIVE_INFINITY;
        for (Generation g : getMoreThan()) {
            if (g.getValue() > max) {
                max = g.getValue();
            }
        }
        return max;
    }

    /**
     * Try to update a parameter
     * @return 0 if parameter updated, 1 if range violated, 2 if conservation violated, 3 if migration violated
     */
    public int tryUpdate(double increment) {
        return 0;
    }

    /**
     * @return the isOffset
     */
    public boolean isIsOffset() {
        return isOffset;
    }

    /**
     * @param isOffset the isOffset to set
     */
    public void setIsOffset(boolean isOffset) {
        this.isOffset = isOffset;
    }

    /**
     * @return the offsetGeneration
     */
    public Generation getOffsetGeneration() {
        return offsetGeneration;
    }

    /**
     * @param offsetGeneration the offsetGeneration to set
     */
    public void setOffsetGeneration(Generation offsetGeneration) {
        this.offsetGeneration = offsetGeneration;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @return the nodes
     */
    public HashSet<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(HashSet<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the lessThan
     */
    public HashSet<Generation> getLessThan() {
        return lessThan;
    }

    /**
     * @param lessThan the lessThan to set
     */
    public void setLessThan(HashSet<Generation> lessThan) {
        this.lessThan = lessThan;
    }

    /**
     * @return the moreThan
     */
    public HashSet<Generation> getMoreThan() {
        return moreThan;
    }

    /**
     * @param moreThan the moreThan to set
     */
    public void setMoreThan(HashSet<Generation> moreThan) {
        this.moreThan = moreThan;
    }

    /**
     * @return the lessThanOffsetNodes
     */
    public HashMap<Generation, Integer> getLessThanOffsetNodes() {
        return lessThanOffsetNodes;
    }

    /**
     * @param lessThanOffsetNodes the lessThanOffsetNodes to set
     */
    public void setLessThanOffsetNodes(HashMap<Generation, Integer> lessThanOffsetNodes) {
        this.lessThanOffsetNodes = lessThanOffsetNodes;
    }

    /**
     * @return the moreThanOffsetNodes
     */
    public HashMap<Generation, Integer> getMoreThanOffsetNodes() {
        return moreThanOffsetNodes;
    }

    /**
     * @param moreThanOffsetNodes the moreThanOffsetNodes to set
     */
    public void setMoreThanOffsetNodes(HashMap<Generation, Integer> moreThanOffsetNodes) {
        this.moreThanOffsetNodes = moreThanOffsetNodes;
    }
}
