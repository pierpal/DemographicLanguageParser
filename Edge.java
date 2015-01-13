package demographicLanguageParser;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a population in the population graph.
 * Edge has start/end time and start/end size.
 * If the size is the same variable, the population has constant size.
 * Exponential growth/decrease is assumed otherwise.
 * If start/end time are the same, this is an instantaneous edge (has time-length epsilon).
 * Migrations that involve this kind of edges are processed first.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Edge implements Comparable<Edge> {

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

    // name of the edge
    private String id;
    // start/end size
    private Node n1;
    // start/end size
    private Node n2;
    // start/end size
    private Size s1;
    // start/end size
    private Size s2;
    // migration out
    private TreeMap<Edge, Migration> migOut = new TreeMap<Edge, Migration>();
    // migration in
    private TreeMap<Edge, Migration> migIn = new TreeMap<Edge, Migration>();
    // counter of created objects
    private static int created = 0;

    /**
     * Builds an edge
     * @param n1 Start node
     * @param n2 End node
     * @param s1 Start size
     * @param s2 End size
     */
    public Edge(Node n1, Node n2, Size s1, Size s2) {
        this.n1 = n1;
        this.n2 = n2;
        this.s1 = s1;
        this.s2 = s2;
        n1.getGen().addLessThan(n2.getGen());
        n2.getGen().addMoreThan(n1.getGen());
        created++;
    }

    /**
     * Sets edge name
     * @param id The name
     */
    public void setName(String id) {
        this.setId(id);
    }

    /**
     * Return population size at specific time.
     * @param time the required time
     * @return size at required time
     */
    public double getSizeAt(double time) throws Exception {
        if (time < getN1().getGen().getValue() || time > getN2().getGen().getValue()) {
            throw new Exception(time + " is not within edge range.");
        }
        if (getS1() == getS2()) {
            return getS1().getValue();
        } else {
            double G = getN2().getGen().getValue() - getN1().getGen().getValue();
            double C = getS1().getValue();
            double A = getS2().getValue();
            return C * Math.pow((A / C), time / G);
        }
    }

    /**
     * Adds an incoming migration
     * @param m The migration
     */
    public void addMigIn(Edge e, Migration m) {
        getMigIn().put(e, m);
//        System.out.println("Added. " + this.migIn.size());
    }

    /**
     * get method for the set of incoming migrations
     * @return the set of incoming migrations
     */
    public TreeMap<Edge, Migration> getMigIn() {
        return migIn;
    }

    /**
     * Adds an outgoing migration
     * @param m The migration
     */
    public void addMigOut(Edge e, Migration m) {
        getMigOut().put(e, m);
//        System.out.println("Added. " + this.migOut.size());
    }

    /**
     * get method for the set of outgoing migrations
     * @return the set of outgoing migrations
     */
    public TreeMap<Edge, Migration> getMigOut() {
        return migOut;
    }

    /**
     * Prints edge details
     */
    @Override
    public String toString() {
        return this.getId() + ":" + getN1().toString() + "->" + getN2().toString();
    }

    public static String edgeSetToString(TreeSet<Edge> edgeSet) {
        if (edgeSet.size() == 0) {
            return "";
        }
        Iterator edgeIt = edgeSet.iterator();
        Edge e = (Edge) edgeIt.next();
        String s = e.getId();
        while (edgeIt.hasNext()) {
            e = (Edge) edgeIt.next();
            s += "-" + e.getId();
        }
        return s;
    }

    /**
     * returns true if the edge is instantaneous
     * @return true if the edge is instantaneous
     */
    public boolean isInstantaneous() {
        return this.getN1().getGen() == this.getN2().getGen();
    }

    /**
     * Implements comparator
     * @return +1 if e2 smaller, -1 if greater, 0 if same id
     */
    @Override
    public int compareTo(Edge e2) {
        if (this.getN2().getGen().getValue() > e2.getN2().getGen().getValue()) {
            return 1;
        } else if (this.getN2().getGen().getValue() < e2.getN2().getGen().getValue()) {
            return -1;
        } else {
            return this.getId().compareTo(e2.getId());
        }
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
     * @return the n1
     */
    public Node getN1() {
        return n1;
    }

    /**
     * @param n1 the n1 to set
     */
    public void setN1(Node n1) {
        this.n1 = n1;
    }

    /**
     * @return the n2
     */
    public Node getN2() {
        return n2;
    }

    /**
     * @param n2 the n2 to set
     */
    public void setN2(Node n2) {
        this.n2 = n2;
    }

    /**
     * @return the s1
     */
    public Size getS1() {
        return s1;
    }

    /**
     * @param s1 the s1 to set
     */
    public void setS1(Size s1) {
        this.s1 = s1;
    }

    /**
     * @return the s2
     */
    public Size getS2() {
        return s2;
    }

    /**
     * @param s2 the s2 to set
     */
    public void setS2(Size s2) {
        this.s2 = s2;
    }

    /**
     * @param migOut the migOut to set
     */
    public void setMigOut(TreeMap<Edge, Migration> migOut) {
        this.migOut = migOut;
    }

    /**
     * @param migIn the migIn to set
     */
    public void setMigIn(TreeMap<Edge, Migration> migIn) {
        this.migIn = migIn;
    }
}

/**
 * Holds two edges. Used as index in hashmaps
 */
class EdgePair {

    // the two edges
    Edge e1, e2;

    /**
     * Constructor.
     * @param e1 first edge
     * @param e2 second edge
     */
    public EdgePair(Edge e1, Edge e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
