package demographicLanguageParser;

import java.util.HashSet;
import java.util.TreeSet;

/**
 * In the population graph a node is a junction across edges where events such as
 * merge, split, change in growth rate and migration may occur.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Node implements Comparable<Node> {

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

    // name of node
    private String id;
    // time of node
    private Generation gen;
    // all sizes in node
    private TreeSet<Size> sizes = new TreeSet<Size>();
    // pivots have precedence in balancing added/removed mass if edge in/out size is conserved
    private TreeSet<Size> pivot = new TreeSet<Size>();
    // incoming edges
    private HashSet<Edge> inEdges = new HashSet<Edge>();
    // outgoing edges
    private HashSet<Edge> outEdges = new HashSet<Edge>();
    // if true, sum of incoming population sizes must be same as outgoing
    private boolean conservation = false;
    // counter of created objects
    private static int created = 0;

    /**
     * Builds a node
     * @param gen generation of node
     */
    public Node(Generation gen) {
        this.gen = gen;
        this.gen.getNodes().add(this);
        created++;
    }

    /**
     * Sets node name
     * @param id The name
     */
    public void setName(String id) {
        this.setId(id);
    }

    /**
     * Adds a size parameter to node size list
     * @param s The size
     */
    public void addSize(Size s) {
        getSizes().add(s);
    }

    /**
     * Adds an incoming edge
     * @param e The edge
     */
    public void addInEdge(Edge e) {
        getInEdges().add(e);
    }

    /**
     * Adds an outgoing edge
     * @param e The edge
     */
    public void addOutEdge(Edge e) {
        getOutEdges().add(e);
    }

    /**
     * Adds a pivot to the node
     * @param p The size pivot
     */
    public void addPivot(Size s) throws Exception {
        if (!sizes.contains(s)) {
            throw new Exception("Trying to add pivot " + s.getId() + ", but it is not part of node " + this.getId());
        }
        if (s.isConstant) {
            throw new Exception("Trying to add a constant size as pivot " + s.getId());
        }
        getSizes().remove(s);
        getPivot().add(s);
    }

    /**
     * returns sum of incoming sizes
     * @return the sum of incoming sizes
     */
    public double getSumOfIncoming() {
        double in = 0.0;
        for (Edge e : getInEdges()) {
            in += e.getS2().getValue();
        }
        return in;
    }

    /**
     * returns sum of outGoing sizes
     * @return the sum of outGoing sizes
     */
    public double getSumOfOutGoing() {
        double out = 0.0;
        for (Edge e : getOutEdges()) {

            out += e.getS1().getValue();
        }
        return out;
    }

    /**
     * checks if an outgoing edge is instantaneous
     * @return true if one outgoing edge is instantaneous
     */
    public boolean hasInstantaneousEdgesInOutput() {
        for (Edge e : this.getOutEdges()) {
            if (e.getN1().getGen() == e.getN2().getGen()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints node details
     * @return the string version
     */
    @Override
    public String toString() {
        return getId() + "(" + getGen().getValue() + ")(" + getInEdges().size() + "_" + getOutEdges().size() + ")";
    }

    /**
     * Implements comparator
     * @return comparison of id strings
     */
    @Override
    public int compareTo(Node n2) {
        return this.getId().compareTo(n2.getId());
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

    /**
     * @return the gen
     */
    public Generation getGen() {
        return gen;
    }

    /**
     * @param gen the gen to set
     */
    public void setGen(Generation gen) {
        this.gen = gen;
    }

    /**
     * @return the sizes
     */
    public TreeSet<Size> getSizes() {
        return sizes;
    }

    /**
     * @param sizes the sizes to set
     */
    public void setSizes(TreeSet<Size> sizes) {
        this.sizes = sizes;
    }

    /**
     * @return the pivot
     */
    public TreeSet<Size> getPivot() {
        return pivot;
    }

    /**
     * @param pivot the pivot to set
     */
    public void setPivot(TreeSet<Size> pivot) {
        this.pivot = pivot;
    }

    /**
     * @return the inEdges
     */
    public HashSet<Edge> getInEdges() {
        return inEdges;
    }

    /**
     * @param inEdges the inEdges to set
     */
    public void setInEdges(HashSet<Edge> inEdges) {
        this.inEdges = inEdges;
    }

    /**
     * @return the outEdges
     */
    public HashSet<Edge> getOutEdges() {
        return outEdges;
    }

    /**
     * @param outEdges the outEdges to set
     */
    public void setOutEdges(HashSet<Edge> outEdges) {
        this.outEdges = outEdges;
    }

    /**
     * @return the conservation
     */
    public boolean isConservation() {
        return conservation;
    }

    /**
     * @param conservation the conservation to set
     */
    public void setConservation(boolean conservation) {
        this.conservation = conservation;
    }
}
