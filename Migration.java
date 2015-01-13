package demographicLanguageParser;

/**
 * Migration between two populations.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Migration {

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

    // name of migration event
    private String id;
    // the rate parameter
    private Edge fromEdge;
    // counter of created objects
    private Edge toEdge;
    private Rate r;
    private static int created = 0;

    /**
     * Constructor
     * @param e1 the first population
     * @param e2 the second population
     * @param r the migration rate
     */
    public Migration(Edge e1, Edge e2, Rate r) {
        this.fromEdge = e1;
        this.toEdge = e2;
        this.r = r;
        if (e1 != e2) {
            e1.addMigOut(e2, this);
            e2.addMigIn(e1, this);
        }
        created++;
    }

    /**
     * Set the parameter name
     * @param id The name
     */
    public void setName(String id) {
        this.setId(id);
    }

    /**
     * toString
     * @return string for object
     */
    @Override
    public String toString() {
        return this.getId() + ":" + this.getFromEdge().getId() + "->" + this.getToEdge().getId() + "=" + this.getR().toString();
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
     * @return the fromEdge
     */
    public Edge getFromEdge() {
        return fromEdge;
    }

    /**
     * @param fromEdge the fromEdge to set
     */
    public void setFromEdge(Edge fromEdge) {
        this.fromEdge = fromEdge;
    }

    /**
     * @return the toEdge
     */
    public Edge getToEdge() {
        return toEdge;
    }

    /**
     * @param toEdge the toEdge to set
     */
    public void setToEdge(Edge toEdge) {
        this.toEdge = toEdge;
    }

    /**
     * @return the r
     */
    public Rate getR() {
        return r;
    }

    /**
     * @param r the r to set
     */
    public void setR(Rate r) {
        this.r = r;
    }

}
