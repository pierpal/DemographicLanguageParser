package demographicLanguageParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Contains a demographic object and functions to parse and query it.
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class DemographicLanguageParser {

    // if true additinal debug information is printed
    private static boolean debug = false;

    /**
     * @return the debug
     */
    public static boolean debugIsOn() {
        return debug;
    }

    /**
     * @param aDebug the debug to set
     */
    public static void setDebug(boolean aDebug) {
        debug = aDebug;
    }
    // *********************************************
    // indexes, to access all objects
    // *********************************************
    // index of all Generation objects
    private HashMap<String, Generation> generations = new HashMap<String, Generation>();
    // index of all Size objects
    private HashMap<String, Size> sizes = new HashMap<String, Size>();
    // index of all Rate objects
    private HashMap<String, Rate> rates = new HashMap<String, Rate>();
    // index of all Node objects
    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    // index of all Edge objects
    private HashMap<String, Edge> edges = new HashMap<String, Edge>();
    // index of all Migration objects
    private HashMap<EdgePair, Migration> migrations = new HashMap<EdgePair, Migration>();
    // index of all Generation objects without name. Integer will be used to name it later.
    private HashMap<Integer, Generation> generationsNoName = new HashMap<Integer, Generation>();
    // index of all Size objects without name. Integer will be used to name it later.
    private HashMap<Integer, Size> sizesNoName = new HashMap<Integer, Size>();
    // index of all Rate objects without name. Integer will be used to name it later.
    private HashMap<Integer, Rate> ratesNoName = new HashMap<Integer, Rate>();
    // index of all Node objects without name. Integer will be used to name it later.
    private HashMap<Integer, Node> nodesNoName = new HashMap<Integer, Node>();
    // index of all Edge objects without name. Integer will be used to name it later.
    private HashMap<Integer, Edge> edgesNoName = new HashMap<Integer, Edge>();
    // index of all Migration objects without name. Integer will be used to name it later.
    private HashMap<Integer, Migration> migrationsNoName = new HashMap<Integer, Migration>();
    // *********************************************
    // sets, some of which are sorted
    // *********************************************
    // set of all conserved nodes, used to check constraints
    private TreeSet<Node> conservedNodes = new TreeSet<Node>();
    // set of all Generations, which is used to traverse the demography in chronological order.
    private TreeSet<Generation> generationsSet = new TreeSet<Generation>();
    // set of all variables.
    private HashMap<String, Object> allVariables = new HashMap<String, Object>();
    // set of demographic parameters only
    private HashMap<String, Parameter> parameters = new HashMap<String, Parameter>();
    // set of migration matrices
    private TreeMap<String, MigrationMatrix> migrationMatrices = new TreeMap<String, MigrationMatrix>();
    // set of migration matrices
    private TreeMap<String, MigrationMatrix> transitionMatrices = new TreeMap<String, MigrationMatrix>();
    // time frames, used to compute coalescence prob
    private ArrayList<TimeFrame> frames;
    // *********************************************
    // other variables
    // *********************************************
    // number of populations at generation 0
    private int numStartPops;

    /**
     * Constructor, parses demography from file
     * @param filename name of file containing demography
     * @throws Exception
     */
    public DemographicLanguageParser(String filename) throws Exception {

        if (debug) {
            System.out.println("Parsing demographic model from file " + filename);
        }
        // *********************************************
        // create generations 0 and infinity, constants
        // *********************************************
        Generation G0 = new Generation(0.0, 0);
        G0.setName("G0");
        generations.put("G0", G0);
        allVariables.put("G0", G0);
        Generation Ginf = new Generation(Double.POSITIVE_INFINITY, 0);
        Ginf.setName("Ginf");
        generations.put("Ginf", Ginf);
        allVariables.put("Ginf", Ginf);
        Generation.setG0(G0);
        Generation.setGinf(Ginf);
        // *********************************************
        // create rates 0 and 1, constants
        // *********************************************
        Rate R0 = new Rate(0.0, 0);
        R0.setName("R0");
        rates.put("R0", R0);
        allVariables.put("R0", R0);
        Rate R1 = new Rate(0.0, 0);
        R1.setName("R1");
        rates.put("R1", R1);
        allVariables.put("R1", R1);
        Rate.setR0(R0);
        Rate.setR1(R1);
        // *********************************************
        // open profile file for reading and start parsing
        // *********************************************
        // open
        FileReader fstream = new FileReader(filename);
        BufferedReader in = new BufferedReader(fstream);
        String str;
        // start parsing
        while (true) {
            // get new line, exit if end of file
            str = in.readLine();
            if (str == null) {
                break;
            }
            // remove white spaces
            str = str.replaceAll("\\s", "");
            // remove comments
            if (str.indexOf('#') != -1) {
                str = str.substring(0, str.indexOf('#'));
            }
            // check line ends in ";" if not empty
            if (str.compareTo("") != 0 && !str.endsWith(";")) {
                throw new Exception("Parse error. Missing \";\" in " + str + "?");
            }
            // get all commands, separated by ";"
            String[] commands = str.split(";");
            // parse all commands in this line
            for (String command : commands) {
                // skip if empty
                if (command.compareTo("") != 0) {
                    if (debug) {
                        System.out.println("Parsing\t" + command);
                    }
                    // check parenthetically balanced and get list of arguments in command
                    String[] args = checkParenthesesAndGetArguments(command);
                    if (debug) {
                        System.out.println("Parentheses are OK, found " + args.length + " arguments.");
                        for (String a : args) {
                            System.out.println("\t" + a);
                        }
                    }
                    // initialize a variable name. It may end up being empty, in which case it will be assigned automatically later.
                    String varName = null;
                    // if there is an assignment, separate variable name and command
                    String[] isVar = command.split("=");
                    if (isVar.length == 1) {
                        // no assignment, get command
                        command = isVar[0];
                    } else if (isVar.length == 2) {
                        // assignment, get name and command
                        varName = isVar[0];
                        if (debug) {
                            System.out.println("Variable is called " + varName);
                        }
                        if (allVariables.containsKey(varName)) {
                            throw new Exception("Variable " + varName + " already exists");
                        }
                        command = isVar[1];
                    } else {
                        throw new Exception("Malformed command: " + command);
                    }
                    if (command.startsWith("gen(")) {
                        // the command is the creation of a Generation
                        Generation g = parseGen(args);
                        if (varName != null) {
                            // the name was already specified
                            g.setName(varName);
                            generations.put(g.getId(), g);
                            allVariables.put(g.getId(), g);
                        } else {
                            // the name was not specified, will parse later
                            generationsNoName.put(Generation.getCreated(), g);
                        }
                    } else if (command.startsWith("size(")) {
                        // the command is the creation of a Size
                        Size s = parseSize(args);
                        if (varName != null) {
                            // the name was already specified
                            s.setName(varName);
                            sizes.put(s.getId(), s);
                            allVariables.put(varName, s);
                        } else {
                            // the name was not specified, will parse later
                            sizesNoName.put(Size.getCreated(), s);
                        }
                    } else if (command.startsWith("rate(")) {
                        // the command is the creation of a Rate
                        Rate r = parseRate(args);
                        if (varName != null) {
                            // the name was already specified
                            r.setName(varName);
                            rates.put(r.getId(), r);
                            allVariables.put(varName, r);
                        } else {
                            // the name was not specified, will parse later
                            ratesNoName.put(Rate.getCreated(), r);
                        }
                    } else if (command.startsWith("node(")) {
                        // the command is the creation of a Node
                        Node n = parseNode(args);
                        if (varName != null) {
                            // the name was already specified
                            n.setName(varName);
                            nodes.put(n.getId(), n);
                            allVariables.put(varName, n);
                        } else {
                            // the name was not specified, will parse later
                            nodesNoName.put(Node.getCreated(), n);
                        }
                    } else if (command.startsWith("edge(")) {
                        // the command is the creation of an Edge
                        Edge e = parseEdge(args);
                        if (varName != null) {
                            // the name was already specified
                            e.setName(varName);
                            edges.put(e.getId(), e);
                            allVariables.put(varName, e);
                        } else {
                            // the name was not specified, will parse later
                            edgesNoName.put(Edge.getCreated(), e);
                        }
                    } else if (command.startsWith("migration(")) {
                        // the command is the creation of a Migration
                        int count = Migration.getCreated();
                        ArrayList<Migration> mList = parseMigration(args);
                        if (varName != null) {
                            throw new Exception("migration doesn not return a handle.");
                        } else {
                            // the name was not specified, will parse later
                            for (Migration m : mList) {
                                count++;
                                migrationsNoName.put(count, m);
                            }
                        }
                    } else if (command.startsWith("conservation(")) {
                        parseConservation(args);

                    } else {
                        throw new Exception("Unknown command: " + command);
                    }
                }
            }
        }
        // name unnamed generations
        for (Integer key : generationsNoName.keySet()) {
            String name = "GG" + key;
            // if a generation was already called this way, apend "G" until it's available
            while (allVariables.containsKey(name)) {
                name = "G" + name;
            }
            // set chosen name
            generationsNoName.get(key).setName(name);
            // add to object-specific index and index of all variables
            generations.put(name, generationsNoName.get(key));
            allVariables.put(name, generationsNoName.get(key));
        }
        // name unnamed sizes
        for (Integer key : sizesNoName.keySet()) {
            String name = "SS" + key;
            // if a generation was already called this way, apend "S" until it's available
            while (allVariables.containsKey(name)) {
                name = "S" + name;
            }
            // set chosen name
            sizesNoName.get(key).setName(name);
            // add to object-specific index and index of all variables
            sizes.put(name, sizesNoName.get(key));
            allVariables.put(name, sizesNoName.get(key));
        }
        // name unnamed rates
        for (Integer key : ratesNoName.keySet()) {
            String name = "RR" + key;
            // if a generation was already called this way, apend "R" until it's available
            while (allVariables.containsKey(name)) {
                name = "R" + name;
            }
            // set chosen name
            ratesNoName.get(key).setName(name);
            // add to object-specific index and index of all variables
            rates.put(name, ratesNoName.get(key));
            allVariables.put(name, ratesNoName.get(key));
        }
        // name unnamed nodes
        for (Integer key : nodesNoName.keySet()) {
            String name = "NN" + key;
            // if a generation was already called this way, apend "N" until it's available
            while (allVariables.containsKey(name)) {
                name = "N" + name;
            }
            // set chosen name
            nodesNoName.get(key).setName(name);
            // add to object-specific index and index of all variables
            nodes.put(name, nodesNoName.get(key));
            allVariables.put(name, nodesNoName.get(key));
        }
        // name unnamed edges
        for (Integer key : edgesNoName.keySet()) {
            String name = "EE" + key;
            // if a generation was already called this way, apend "E" until it's available
            while (allVariables.containsKey(name)) {
                name = "E" + name;
            }
            // set chosen name
            edgesNoName.get(key).setName(name);
            // add to object-specific index and index of all variables
            edges.put(name, edgesNoName.get(key));
            allVariables.put(name, edgesNoName.get(key));
        }
        // name unnamed migrations
        for (Integer key : migrationsNoName.keySet()) {
            String name = "MM" + key;
            // if a generation was already called this way, apend "M" until it's available
            while (allVariables.containsKey(name)) {
                name = "M" + name;
            }
            // set chosen name
            migrationsNoName.get(key).setName(name);
            Migration m = migrationsNoName.get(key);
//            // users can't specify chance of not migrating. It's 1 if no outbound migration was specified, otherwise it's the 1-sum(migrations), and is matrix-specific
//            if (m.fromEdge == m.toEdge) {
//                throw new Exception("Tried to specify migration between " + m.fromEdge + " and " + m.toEdge + " but self migration (chance to remain in a population) is entirely defined by chance of migrating to all other populations, and depends on which populations coexist for specific parameter values. Refer to manual for details.");
//            }
            // add to object-specific index and index of all variables
            migrations.put(new EdgePair(m.getFromEdge(), m.getToEdge()), m);
            allVariables.put(name, m);
        }
        // get all active (in edges) Generation variables and put the in the sorted set used to traverse demography.
        for (Edge e : edges.values()) {
            generationsSet.add(e.getN1().getGen());
            generationsSet.add(e.getN2().getGen());
        }
        // add all demographic parameters to index.
        if (debug) {
            System.out.println("Parameters:");
        }
        for (Object o : allVariables.values()) {
            if (o instanceof demographicLanguageParser.Parameter) {
                Parameter p = (Parameter) o;
                parameters.put(p.getId(), p);
                if (debug) {
                    System.out.println("\t" + p.toString());
                }
            }
        }
        // DONE PARSING
        // build migration matrices
        buildMigrationMatrices();
        numStartPops = Generation.getG0().getNodes().size();
    }

    /**
     * Checks the parenthetic balance of a command and returns its arguments
     * @param command string
     * @return array of strings representing command parameters
     */
    private String[] checkParenthesesAndGetArguments(String command) throws Exception {
        // will contain commands
        ArrayList<String> args = new ArrayList<String>();
        // depth of perentheses
        int p = 0;
        // pointer to argument start
        int current = 0;
        for (int i = 0; i < command.length(); i++) {
            if (command.charAt(i) == '(') {
                // increment parenthetical level
                p++;
                // if opening parenthesis, set command start
                if (p == 1) {
                    current = i + 1;
                }
            } else if (command.charAt(i) == ')') {
                // decrement parenthetical level
                p--;
                // if closing parenthesis, get lat command
                if (p == 0) {
                    args.add(command.substring(current, i));
                }
                // if find a comma at level 1, it's an argument
            } else if (command.charAt(i) == ',' && p == 1) {
                args.add(command.substring(current, i));
                // update position of argument start
                current = i + 1;
            }
        }
        // level must be 0 when done, or unbalanced
        if (p != 0) {
            throw new Exception("Command " + command + " has unbalanced parentheses.");
        }
        // return array of parsed commands
        return args.toArray(new String[args.size()]); // NOTE: this cast is for legacy code
    }

    /**
     * Creates a Generation object from a string command
     * @param array of strings representing command arguments (initial value, number of grid points)
     * @return Generation object created from command
     */
    private Generation parseGen(String[] args) throws Exception {
        // arguments must be either 1 (constant) or two (also specify number of grid points, cosntant if 0).
        if (args.length != 1 && args.length != 2) {
            throw new Exception("gen has " + args.length + " arguments. Expecting 1 (constant value) or 2 (initial value, number of initializations).");
        }
        // offset node?
        if (args[0].startsWith("gen(")) {
            if (args.length != 2) {
                throw new Exception("Offset generation must have 2 arguments (generation, offset)");
            }
            // this is an offset node
            // get its arguments
            String[] newArgs = checkParenthesesAndGetArguments(args[0]);
            // create offsetted gen
            Generation offsettedGen = parseGen(newArgs);
            // put it in the set of generations that haven't been named yet
            getGenerationsNoName().put(Generation.getCreated(), offsettedGen);
            // create offset generation
            return new Generation(offsettedGen, Integer.parseInt(args[1]));
        } else if (getGenerations().containsKey(args[0])) {
            if (args.length != 2) {
                throw new Exception("Offset generation must have 2 arguments (generation, offset)");
            }
            // this is an offset node with known offsetted generation
            return new Generation(getGenerations().get(args[0]), Integer.parseInt(args[1]));
        }
        // not an offset node
        double val = Double.parseDouble(args[0]);
        if (Math.abs(val - Math.round(val)) > Parameter.getEpsilon()) {
            throw new Exception("generations must be integer values.");
        }
        if (args.length == 1) {
            return new Generation(val, 0);
        } else {
            return new Generation(val, Integer.parseInt(args[1]));
        }
    }

    /**
     * Creates a Size object from a string command
     * @param array of strings representing command arguments (initial value, number of grid points)
     * @return Size object created from command
     */
    private Size parseSize(String[] args) throws Exception {
        // arguments must be either 1 (constant) or two (also specify number of grid points, cosntant if 0).
        if (args.length == 1) {
            return new Size(Double.parseDouble(args[0]), 0);
        } else if (args.length == 2) {
            return new Size(Double.parseDouble(args[0]), Integer.parseInt(args[1]));
        } else {
            throw new Exception("size has " + args.length + " arguments. Expecting 1 (constant value) or 2 (initial value, number of initializations).");
        }
    }

    /**
     * Creates a Rate object from a string command
     * @param array of strings representing command arguments (initial value, number of grid points)
     * @return Rate object created from command
     */
    private Rate parseRate(String[] args) throws Exception {
        // arguments must be either 1 (constant) or two (also specify number of grid points, cosntant if 0).
        if (args.length == 1) {
            return new Rate(Double.parseDouble(args[0]), 0);
        } else if (args.length == 2) {
            return new Rate(Double.parseDouble(args[0]), Integer.parseInt(args[1]));
        } else {
            throw new Exception("rate has " + args.length + " arguments. Expecting 1 (constant value) or 2 (initial value, number of initializations).");
        }
    }

    /**
     * Creates a Node object from a string command
     * @param array of strings representing command arguments (generation)
     * @return Node object created from command
     */
    private Node parseNode(String[] args) throws Exception {
        // must have 1 argument
        if (args.length == 1) {
            Generation g;
            // it's a gen( command
            if (args[0].startsWith("gen(")) {
                g = parseGen(checkParenthesesAndGetArguments(args[0]));
                getGenerationsNoName().put(Generation.getCreated(), g);
            } else {
                // it's a variable name
                g = getGenerations().get(args[0]);
            }
            // used an unspecified variable name
            if (g == null) {
                throw new Exception("Generation " + args[0] + " was not declared or cannot be parsed.");
            }
            return new Node(g);
        } else {
            throw new Exception("node has " + args.length + " arguments. Expecting 1 (generation).");
        }
    }

    /**
     * Creates an Edge object from a string command
     * @param array of strings representing command arguments (node, node, size) for constant, or (node, node, size, size) for exponential. If second size is specified but same as first, the population is constant
     * @return Edge object created from command
     */
    private Edge parseEdge(String[] args) throws Exception {
        // edge has two nodes and two sizes (same if constant)
        Node n1, n2;
        Size s1, s2;
        // must have 3 or 4 arguments
        if (args.length != 3 && args.length != 4) {
            throw new Exception("edge has " + args.length + " arguments. Expecting 3 (node, node, size) for constant population, or 4 (node, node, size, size) for exponential.");
        }
        // first node is a node( command
        if (args[0].startsWith("node(")) {
            n1 = parseNode(checkParenthesesAndGetArguments(args[0]));
            getNodesNoName().put(Node.getCreated(), n1);
        } else {
            // specified existing node
            n1 = getNodes().get(args[0]);
        }
        // specified node doesn't exist
        if (n1 == null) {
            throw new Exception("Node " + args[0] + " was not declared or cannot be parsed.");
        }
        // second node is a node( command
        if (args[1].startsWith("node(")) {
            n2 = parseNode(checkParenthesesAndGetArguments(args[1]));
            getNodesNoName().put(Node.getCreated(), n2);
        } else {
            // specified existing node
            n2 = getNodes().get(args[1]);
        }
        // specified node doesn't exist
        if (n2 == null) {
            throw new Exception("Node " + args[1] + " was not declared or cannot be parsed.");
        }
        // first size is a size( command
        if (args[2].startsWith("size(")) {
            s1 = parseSize(checkParenthesesAndGetArguments(args[2]));
            getSizesNoName().put(Size.getCreated(), s1);
        } else {
            // specified existing size
            s1 = getSizes().get(args[2]);
        }
        // specified size doesn't exist
        if (s1 == null) {
            throw new Exception("Size " + args[2] + " was not declared or cannot be parsed.");
        }
        // if no other size was specified, use same -> constant
        if (args.length == 3) {
            s2 = s1;
            // a second size was specified (for exponential)
        } else {
            // second size is a size( command
            if (args[3].startsWith("size(")) {
                s2 = parseSize(checkParenthesesAndGetArguments(args[3]));
                getSizesNoName().put(Size.getCreated(), s2);
            } else {
                // specified existing size
                s2 = getSizes().get(args[3]);
            }
            // specified size doesn't exist
            if (s2 == null) {
                throw new Exception("Size " + args[3] + " was not declared or cannot be parsed.");
            }
        }
        // create edge object
        Edge e = new Edge(n1, n2, s1, s2);
        // add sizes to nodes (might be used in conservation constraints)
        n1.addSize(s1);
        n1.addOutEdge(e);
        n2.addSize(s2);
        n2.addInEdge(e);
        return e;
    }

    /**
     * Creates a Migration object from a string command
     * @param array of strings representing command arguments (edge1, edge2, rate)
     * @return array of Migration objects created from command
     */
    private ArrayList<Migration> parseMigration(String[] args) throws Exception {
        // arguments must be 3
        if (!(args.length > 2 && args.length % 2 == 1)) {
            throw new Exception("mig has " + args.length + " arguments. Usage: migration(edgeFrom1, edgeTo1, edgeFrom2, edgeTo2, ..., rate).");
        }
        // rate variable
        Rate r;
        // list of migrations to be returned
        ArrayList<Migration> mList = new ArrayList<Migration>();
        // last argument is a rate( command
        if (args[2].startsWith("rate(")) {
            r = parseRate(checkParenthesesAndGetArguments(args[args.length - 1]));
            getRatesNoName().put(Rate.getCreated(), r);
        } else {
            // specified existing rate
            r = getRates().get(args[args.length - 1]);
        }
        // specified rate doesn't exist
        if (r == null) {
            throw new Exception("Rate " + args[args.length - 1] + " was not declared or cannot be parsed.");
        }
        for (int i = 0; i < args.length - 2; i += 2) {
            // migration is between edges
            Edge e1, e2;
            // first argument is an edge( command
            if (args[i].startsWith("edge(")) {
                e1 = parseEdge(checkParenthesesAndGetArguments(args[i]));
                getEdgesNoName().put(Edge.getCreated(), e1);
            } else {
                // specified existing edge
                e1 = getEdges().get(args[i]);
            }
            // specified edge doesn't exist
            if (e1 == null) {
                throw new Exception("Edge " + args[i] + " was not declared or cannot be parsed.");
            }
            // second argument is an edge( command
            if (args[1].startsWith("edge(")) {
                e2 = parseEdge(checkParenthesesAndGetArguments(args[i + 1]));
                getEdgesNoName().put(Edge.getCreated(), e2);
            } else {
                // specified existing edge
                e2 = getEdges().get(args[i + 1]);
            }
            // specified edge doesn't exist
            if (e2 == null) {
                throw new Exception("Edge " + args[i + 1] + " was not declared or cannot be parsed.");
            }
            // add new migration object
            mList.add(new Migration(e1, e2, r));
        }
        return mList;
    }

    /**
     * Sets a conservation constraint for a node from a string command
     * @param array of strings representing command arguments (node, pivot1, pivot2, ...)
     */
    private void parseConservation(String[] args) throws Exception {
        // at least a node must be specified (pivots are optional)
        if (args.length < 1) {
            throw new Exception("conservation has " + args.length + " arguments. Expecting at least 1 (node, pivot1, pivot2, ...).");
        }
        // the node
        Node n;
        // node is specified via node( command
        if (args[0].startsWith("node(")) {
            n = parseNode(checkParenthesesAndGetArguments(args[0]));
            getNodesNoName().put(Node.getCreated(), n);
        } else {
            // using existing node
            n = getNodes().get(args[0]);
        }
        if (n == null) {
            throw new Exception("Node " + args[0] + " was not declared or cannot be parsed.");
        }
        // set conservation flag in node
        n.setConservation(true);
        getConservedNodes().add(n);
        // if there are mode arguments, they're all pivots
        for (int i = 1; i < args.length; i++) {
            Size s = getSizes().get(args[i]);
            n.addPivot(s);
        }
    }

    /**
     * Prints a description of the demographic object
     */
    public void narrate() {
        System.out.println("Once upon a time ...");
        // will traverse chronologically from present to past. At all times a set of active
        // edges (populations) is kept
        TreeSet<Edge> activeEdges = new TreeSet<Edge>();
        // There are periods of duration epsilon for which instantaneous migration matrices are speficied
        // these are treated differently
        TreeSet<Edge> instantaneousEdges = new TreeSet<Edge>();
        // for all current generations, starting from present, update active edges and describe
        Generation[] currentGenSet = getGenerationsSet().toArray(new Generation[getGenerationsSet().size()]);
        for (int genCnt = 0; genCnt < currentGenSet.length - 1; genCnt++) {
            Generation currentGeneration = currentGenSet[genCnt];
            if (currentGeneration == Generation.getGinf()) {
                break;
            }
            System.out.println("From generation " + currentGeneration.toString() + " to generation " + currentGenSet[genCnt + 1].toString());
            // these are edges that have current generation as destination
            HashSet<Edge> toBeRemoved = new HashSet<Edge>();
            // for all active edges, sorted by terminal node time, check if expired and add to list of edges to be removed from active
            for (Edge e : activeEdges) {
                // next edge destination is larger, remove edge and break, since they're sorted
                if (currentGeneration.compareTo(e.getN2().getGen()) == -1) {
                    // since they're sorted, all others are still active
                    break; // no need to check further
                } else {
                    // gen is not larger, remove inactive edge
                    System.out.println(e.toString() + " stopped being active.");
                    toBeRemoved.add(e);
                }
            }
            // remove expired
            activeEdges.removeAll(toBeRemoved);
            // for all nodes of this generation, get new outgoing edges to be activated
            System.out.println("Some populations appeared:");
            for (Node n : currentGeneration.getNodes()) {
                for (Edge eOut : n.getOutEdges()) {
                    System.out.println("\t" + eOut.toString());
                    // the end of the edge is the same as the start, it's instantaneous
                    if (eOut.getN1().getGen().getValue() == eOut.getN2().getGen().getValue()) {
                        instantaneousEdges.add(eOut);
                    } else {
                        // this one is normal
                        activeEdges.add(eOut);
                    }
                }
            }
            // take care of instantaneour edges:
            for (Edge e : instantaneousEdges) {
                System.out.println("Instantaneous transition at " + currentGeneration.getId() + " " + e.getId() + " ->");
                double sum = e.getN2().getSumOfOutGoing();
                for (Edge eo : e.getN2().getOutEdges()) {
                    System.out.println(eo.getId() + " prob. " + eo.getS1().getValue() / sum);
                }
            }
            // clean instantaneous matrix for next iteration
            instantaneousEdges.clear();
            // now all others
            // don't bother building a matrix with one element
            if (activeEdges.size() > 1) {
                System.out.println("Some populations were active, but not instantaneous: " + Edge.edgeSetToString(activeEdges));
                MigrationMatrix migMat;
                // create string that is used to index migration matrices
                String activeEdgeString = Edge.edgeSetToString(activeEdges);
                migMat = getMigrationMatrices().get(activeEdgeString);
                boolean worthPrinting = false;
                for (int i = 0; i < migMat.numPopIn; i++) {
                    if (migMat.getMigMatrix().getMat()[i][i] < 1 - Parameter.getEpsilon()) {
                        worthPrinting = true;
                        break;
                    }
                }
                if (!worthPrinting) {
                    System.out.println("With no migration");
                } else {
                    System.out.println("These had a migration matrix:\n" + migMat.toString());
                }
            } else {
                System.out.println("No migration: only one population");
            }
        }
    }

    /**
     * Builds migration matrices
     */
    public void buildMigrationMatrices() throws Exception {
        ArrayList<TimeFrame> frames = new ArrayList<TimeFrame>();
        if (debugIsOn()) {
            System.out.println("Building migration matrices");
        }
        // will traverse chronologically from present to past. At all times a set of active
        // edges (populations) is kept
        TreeSet<Edge> activeEdges = new TreeSet<Edge>();
        // There are periods of duration epsilon for which instantaneous migratino matrices are speficied
        // these are treated differently
        TreeSet<Edge> instantaneousEdges = new TreeSet<Edge>();
        // for all current generations, starting from present, update active edges and describe
        Generation[] currentGenSet = getGenerationsSet().toArray(new Generation[getGenerationsSet().size()]);
        for (int genCnt = 0; genCnt < currentGenSet.length - 1; genCnt++) {
            Generation currentGeneration = currentGenSet[genCnt];
            if (currentGeneration == Generation.getGinf()) {
                break;
            }
            if (debugIsOn()) {
                System.out.println("- From generation " + currentGenSet[genCnt].toString() + " to generation " + currentGenSet[genCnt + 1].toString());
            }
            // these are edges that have current generation as destination
            TreeSet<Edge> toBeRemoved = new TreeSet<Edge>();
            // for all active edges, sorted by terminal node time, check if expired and add to list of edges to be removed from active
            for (Edge e : activeEdges) {
                if (debugIsOn()) {
                    System.out.println("checking out " + e);
                }
                // next edge destination is larger, remove edge and break, since they're sorted
                if (currentGeneration.compareTo(e.getN2().getGen()) == -1) {
                    // since they're sorted, all others are still active
                    if (debugIsOn()) {
                        System.out.println("BREAK");
                    }
                    break; // no need to check further
                } else {
                    // gen is not larger, remove inactive edge
                    if (debugIsOn()) {
                        System.out.println("\tremoved " + e.toString());
                    }
                    toBeRemoved.add(e);
                }
            }
            // remove expired
            activeEdges.removeAll(toBeRemoved);
            // take care of normal transitions
            if (debugIsOn()) {
                for (Edge e : toBeRemoved) {
                    double sum = e.getN2().getSumOfOutGoing();
                    if (debugIsOn()) {
                        System.out.println("Standard transition at " + currentGeneration.getId() + " " + e.getId() + " ->");
                        for (Edge eo : e.getN2().getOutEdges()) {
                            System.out.println("\t" + eo.getId() + " prob. " + eo.getS1().getValue() / sum);
                        }
                    }
                }
            }
            if (toBeRemoved.size() > 0) {
                MigrationMatrix transMat = new MigrationMatrix(toBeRemoved, activeEdges);
                if (debugIsOn()) {
                    System.out.println("Transition " + transMat.toString());
                }
                String matrixID = Edge.edgeSetToString(instantaneousEdges) + "_" + Edge.edgeSetToString(activeEdges);
                getTransitionMatrices().put(matrixID, transMat);
                // this transition is for last frame
                frames.get(frames.size() - 1).addTransition(transMat);
            }
            // these will be added after instantaneous edges were processed
            TreeSet<Edge> toBeAdded = new TreeSet<Edge>();
            // for all nodes of this generation, get new outgoing edges to be activated
            for (Node n : currentGeneration.getNodes()) {
                for (Edge eOut : n.getOutEdges()) {
                    if (debugIsOn()) {
                        System.out.println("\tadded " + eOut.toString());
                    }
                    // the end of the edge is the same as the start, it's instantaneous
                    if (eOut.getN1().getGen().getValue() == eOut.getN2().getGen().getValue()) {
                        instantaneousEdges.add(eOut);
                    } else {
                        // this one is normal
                        toBeAdded.add(eOut);
                    }
                }
            }
            // now add edges to active
            activeEdges.addAll(toBeAdded);
            // take care of instantaneour edges
            if (debugIsOn()) {
                for (Edge e : instantaneousEdges) {
                    double sum = e.getN2().getSumOfOutGoing();
                    if (debugIsOn()) {
                        System.out.println("Instantaneous transition at " + currentGeneration.getId() + " " + e.getId() + " ->");
                        for (Edge eo : e.getN2().getOutEdges()) {
                            System.out.println("\t" + eo.getId() + " prob. " + eo.getS1().getValue() / sum);
                        }
                    }
                }
            }
            if (instantaneousEdges.size() > 0) {
                MigrationMatrix transMat = new MigrationMatrix(instantaneousEdges, activeEdges);
                if (debugIsOn()) {
                    System.out.println("Transition " + transMat.toString());
                }
                String matrixID = Edge.edgeSetToString(instantaneousEdges) + "_" + Edge.edgeSetToString(activeEdges);
                getTransitionMatrices().put(matrixID, transMat);
                // this transition is for last frame
                frames.get(frames.size() - 1).addTransition(transMat);
            }
            // clean instantaneous matrix for next iteration
            instantaneousEdges.clear();
            if (debugIsOn()) {
                System.out.println("Now migration from " + currentGeneration.getValue() + " to " + currentGenSet[genCnt + 1].getValue());
            }            // now migrations for this range others
            // don't bother building a matrix with one element
            MigrationMatrix migMat;
            // create string that is used to index migration matrices
            String activeEdgeString = Edge.edgeSetToString(activeEdges);
            // make it and put it in hashmap
            migMat = new MigrationMatrix(activeEdges);
            getMigrationMatrices().put(activeEdgeString, migMat);
            TimeFrame curentFrame = new TimeFrame(currentGenSet[genCnt], currentGenSet[genCnt + 1], migMat);
            if (debugIsOn()) {
                System.out.println("Matrix was created:\n" + migMat.toString());
            }
            for (int g = (int) Math.round(currentGeneration.getValue()) + 1; g <= (int) Math.round(currentGenSet[genCnt + 1].getValue()); g++) {
            }
            if (debugIsOn()) {
                System.out.println("END period");
            }
            frames.add(curentFrame);
        }
        this.setFrames(frames);
    }

    /**
     * Check if all ranges, conservation and migration matrix constraints are satisfied
     * @return true if all constraints satisfied
     */
    public boolean checkConstraints() throws Exception {
        checkGenerationsRange();
        checkSizeRange();
        checkMigrationRange();
        checkNodesConservation();
        checkMigrationMatricesSumTo1();
        // will throw exception otherwise
        // now check that all nodes have outgoing edges (if not at Ginf)
        for (Node n : getNodes().values()) {
            if (n.getOutEdges().size() == 0 && n.getGen() != Generation.getGinf()) {
                throw new Exception("All nodes but the one at Ginf have to have at least one outgoing edge.");
            }
            if (n.getGen().getValue() == 0 && n.getOutEdges().size() != 1) {
                throw new Exception("All nodes at 0 must have only one outgoing population.");
            }
        }
        // now check that all edges to Ginf are constant
        for (Edge e : getEdges().values()) {
            if (e.getN2().getGen() == Generation.getGinf() && e.getS1() != e.getS2()) {
                throw new Exception("All edges connected to a node at Ginf must have constant size.");
            }
            if (e.isInstantaneous() && e.getN1().getGen().getValue() == 0) {
                throw new Exception("Cannot have instantaneous edges at generation 0.");
            }
            if (e.isInstantaneous() && e.getN2().hasInstantaneousEdgesInOutput()) {
                throw new Exception("Instantaneous edges cannot be connected to nodes that have other instantaneous edges in output.");
            }
        }
        if (debugIsOn()) {
            System.out.println("All constraints OK");
        }
        return true;
    }

    /**
     * Check if all generations are in range
     * @return true if all generations are in range
     */
    private boolean checkGenerationsRange() throws Exception {
        for (Generation g : getGenerations().values()) {
            if (g != Generation.getGinf() && (g.getValue() <= g.getAtLeast() || g.getValue() >= g.getAtMost())) {
                throw new Exception("generation " + g.toString() + " in not within open interval " + g.getAtLeast()
                        + " to " + g.getAtMost() + ".");
            }
        }
        if (debugIsOn()) {
            System.out.println("Generations range OK");
        }
        return true;
    }

    /**
     * Check if all sizes are in range
     * @return true if all sizes are in range
     */
    private boolean checkSizeRange() throws Exception {
        for (Size s : getSizes().values()) {
            if (s.getValue() < s.getMin() || s.getValue() > s.getMax()) {
                throw new Exception("size " + s.getId() + " in not within range " + s.getMin() + " to " + s.getMax() + ".");
            }
        }
        if (debugIsOn()) {
            System.out.println("Sizes range OK");
        }
        return true;
    }

    /**
     * Check if all migration rates are in range
     * @return true if all migration rates are in range
     */
    private boolean checkMigrationRange() throws Exception {
        for (Rate r : getRates().values()) {
            if (r.getValue() < r.getMin() || r.getValue() > r.getMax()) {
                throw new Exception("rate " + r.getId() + " in not within range. " + r.getMin() + " to " + r.getMax() + ".");
            }
        }
        if (debugIsOn()) {
            System.out.println("Migrations range OK");
        }
        return true;
    }

    /**
     * Check if all conserved nodes have input=output
     * @return true if all conserved nodes have input=output
     */
    private boolean checkNodesConservation() throws Exception {
        for (Node n : getConservedNodes()) {
            if (Math.abs(n.getSumOfIncoming() - n.getSumOfOutGoing()) > Parameter.getEpsilon()) {
                throw new Exception("Violaed conservation constraint for node " + n.getId() + " incoming: " + n.getSumOfIncoming() + " outgoing: " + n.getSumOfOutGoing());
            }
        }
        if (debugIsOn()) {
            System.out.println("Node conservation OK");
        }
        return true;
    }

    /**
     * Check if all rows of all migration matrices sum to 1
     * @return true if all rows of all migration matrices sum to 1, false otherwise
     */
    private boolean checkMigrationMatricesSumTo1() throws Exception {
        for (MigrationMatrix m : getMigrationMatrices().values()) {
            Double[][] migMat = m.getMigMatrix().getMat();
            for (int i = 0; i < m.getNumPopIn(); i++) {
                double sumRow = 0.0;
                for (int j = 0; j < m.getNumPopOut(); j++) {
                    sumRow += migMat[i][j];
                }
                if (Math.abs(sumRow - 1.0) > Parameter.getEpsilon()) {
                    throw new Exception("Violated constraint in migration matrix. Matrix row " + (1 + i) + " sums to " + sumRow + "\t" + m.toString());
                }
            }
        }
        if (debugIsOn()) {
            System.out.println("Migration matrices OK");
        }
        return true;
    }

    /**
     * get number of populations at generation 0
     * @return number of populations at generation 0
     */
    public Edge[] getPopsAt0() {
        return getFrames().get(0).migration.populationsFrom;
    }

    /**
     * Compute coalescent probability, or expected fraction of genome shared, or expected number of segments in range
     * @return matrix with quantity for each population pair
     */
    public Matrix computeModelQuantity(QuantityType type, double[] args) throws Exception {
        if (type == QuantityType.expectedFraction && args.length != 2) {
            throw new Exception(type + " requires 2 arguments for the computation: from length, to length.");
        } else if (type == QuantityType.expectedNumberOfSegments && args.length != 3) {
            throw new Exception(type + " requires 3 arguments for the computation: from length, to length, genome size.");
        } else if (type == QuantityType.coalescent && args.length != 2) {
            throw new Exception(type + " requires 2 arguments for the computation: from generation, to generation.");
        } else if (type == QuantityType.coalescent && (args[0] < 0 || args[1] > TimeFrame.MAX_TIME)) {
            throw new Exception(type + " coalescent generation must be between 0 and " + TimeFrame.MAX_TIME);
        }

        Matrix state = new Matrix(getNumStartPops());
        double[][] probCoal = new double[getNumStartPops()][getNumStartPops()];
        double[][] probNotCoal = new double[getNumStartPops()][getNumStartPops()];
        double[][] res = new double[getNumStartPops()][getNumStartPops()];
        for (int i = 0; i < getNumStartPops(); i++) {
            for (int j = 0; j < getNumStartPops(); j++) {
                probNotCoal[i][j] = 1.0;
            }
        }
        int g = 0;
        for (int i = 0; i < getFrames().size(); i++) {
            TimeFrame frame = getFrames().get(i);
            // TODO: implement efficient approach for coalescent query
            if (frame.migration.populationsTo.length == 1 && frame.intEnd == TimeFrame.MAX_TIME) {
                if (type == QuantityType.coalescent) {
                    System.err.println("Warning: efficient method not implemented. Using iterative method.");
                } else if (type == QuantityType.expectedFraction) {
                    System.err.println("Warning: efficient method not implemented. Using iterative method.");
                } else {
                    double A = frame.migration.populationsTo[0].getN1().getSumOfOutGoing();
                    double maxGenD = (double) frame.intStart + 0.5; // integral starts from maxGen+1/2
                    double u = args[0];
                    double v = args[1];
                    double G = args[2];
                    double val = (2 * G * (A + maxGenD + 2 * A * maxGenD * u)) / (Math.exp(maxGenD
                            * (1 / A + 2 * u)) * Math.pow((1 + 2 * A * u), 2)) - (2 * G * (A + maxGenD + 2 * A * maxGenD * v))
                            / (Math.exp(maxGenD * (1 / A + 2 * v)) * Math.pow((1 + 2 * A * v), 2));
                    for (int k = 0; k < getNumStartPops(); k++) {
                        for (int t = 0; t < getNumStartPops(); t++) {
                            res[k][t] += val;
                        }
                    }
                    return new Matrix(res);
                }
            }
            Matrix[] coal = frame.computeCoalescenceVector(state);
            for (int j = 0; j < coal.length; j++) {
                g++;
                double factor;
                if (debugIsOn()) {
                    System.out.println("gen: " + g);
                    coal[j].printMat();
                }
                if (type == QuantityType.coalescent) {
                    factor = (g >= args[0] && g <= args[1]) ? 1.0 : 0.0;
                } else if (type == QuantityType.expectedFraction) {
                    factor = 1. / 50. * (Math.exp(-g * args[0] / 50.) * (50. + g * args[0])
                            - Math.exp(-g * args[1] / 50.) * (50. + g * args[1]));
                } else {
                    factor = 2 * (Math.exp(-2 * g * args[0]) - Math.exp(-2 * g * args[1])) * args[2] * g;
                }
                for (int k = 0; k < getNumStartPops(); k++) {
                    for (int t = 0; t < getNumStartPops(); t++) {
                        probCoal[k][t] += coal[j].getMat()[k][t] * probNotCoal[k][t];
                        res[k][t] += coal[j].getMat()[k][t] * probNotCoal[k][t] * factor;
                        probNotCoal[k][t] = 1.0 - probCoal[k][t];
                    }
                }
            }
            if (debugIsOn()) {
                state.printMat();
                System.out.println("gen: " + g);
                Matrix.printMat(probCoal);
            }
        }
        return new Matrix(res);
    }

    /**
     * @return the generations
     */
    public HashMap<String, Generation> getGenerations() {
        return generations;
    }

    /**
     * @param generations the generations to set
     */
    public void setGenerations(HashMap<String, Generation> generations) {
        this.generations = generations;
    }

    /**
     * @return the sizes
     */
    public HashMap<String, Size> getSizes() {
        return sizes;
    }

    /**
     * @param sizes the sizes to set
     */
    public void setSizes(HashMap<String, Size> sizes) {
        this.sizes = sizes;
    }

    /**
     * @return the rates
     */
    public HashMap<String, Rate> getRates() {
        return rates;
    }

    /**
     * @param rates the rates to set
     */
    public void setRates(HashMap<String, Rate> rates) {
        this.rates = rates;
    }

    /**
     * @return the nodes
     */
    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(HashMap<String, Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the edges
     */
    public HashMap<String, Edge> getEdges() {
        return edges;
    }

    /**
     * @param edges the edges to set
     */
    public void setEdges(HashMap<String, Edge> edges) {
        this.edges = edges;
    }

    /**
     * @return the migrations
     */
    public HashMap<EdgePair, Migration> getMigrations() {
        return migrations;
    }

    /**
     * @param migrations the migrations to set
     */
    public void setMigrations(HashMap<EdgePair, Migration> migrations) {
        this.migrations = migrations;
    }

    /**
     * @return the generationsNoName
     */
    public HashMap<Integer, Generation> getGenerationsNoName() {
        return generationsNoName;
    }

    /**
     * @param generationsNoName the generationsNoName to set
     */
    public void setGenerationsNoName(HashMap<Integer, Generation> generationsNoName) {
        this.generationsNoName = generationsNoName;
    }

    /**
     * @return the sizesNoName
     */
    public HashMap<Integer, Size> getSizesNoName() {
        return sizesNoName;
    }

    /**
     * @param sizesNoName the sizesNoName to set
     */
    public void setSizesNoName(HashMap<Integer, Size> sizesNoName) {
        this.sizesNoName = sizesNoName;
    }

    /**
     * @return the ratesNoName
     */
    public HashMap<Integer, Rate> getRatesNoName() {
        return ratesNoName;
    }

    /**
     * @param ratesNoName the ratesNoName to set
     */
    public void setRatesNoName(HashMap<Integer, Rate> ratesNoName) {
        this.ratesNoName = ratesNoName;
    }

    /**
     * @return the nodesNoName
     */
    public HashMap<Integer, Node> getNodesNoName() {
        return nodesNoName;
    }

    /**
     * @param nodesNoName the nodesNoName to set
     */
    public void setNodesNoName(HashMap<Integer, Node> nodesNoName) {
        this.nodesNoName = nodesNoName;
    }

    /**
     * @return the edgesNoName
     */
    public HashMap<Integer, Edge> getEdgesNoName() {
        return edgesNoName;
    }

    /**
     * @param edgesNoName the edgesNoName to set
     */
    public void setEdgesNoName(HashMap<Integer, Edge> edgesNoName) {
        this.edgesNoName = edgesNoName;
    }

    /**
     * @return the migrationsNoName
     */
    public HashMap<Integer, Migration> getMigrationsNoName() {
        return migrationsNoName;
    }

    /**
     * @param migrationsNoName the migrationsNoName to set
     */
    public void setMigrationsNoName(HashMap<Integer, Migration> migrationsNoName) {
        this.migrationsNoName = migrationsNoName;
    }

    /**
     * @return the conservedNodes
     */
    public TreeSet<Node> getConservedNodes() {
        return conservedNodes;
    }

    /**
     * @param conservedNodes the conservedNodes to set
     */
    public void setConservedNodes(TreeSet<Node> conservedNodes) {
        this.conservedNodes = conservedNodes;
    }

    /**
     * @return the generationsSet
     */
    public TreeSet<Generation> getGenerationsSet() {
        return generationsSet;
    }

    /**
     * @param generationsSet the generationsSet to set
     */
    public void setGenerationsSet(TreeSet<Generation> generationsSet) {
        this.generationsSet = generationsSet;
    }

    /**
     * @return the allVariables
     */
    public HashMap<String, Object> getAllVariables() {
        return allVariables;
    }

    /**
     * @param allVariables the allVariables to set
     */
    public void setAllVariables(HashMap<String, Object> allVariables) {
        this.allVariables = allVariables;
    }

    /**
     * @return the parameters
     */
    public HashMap<String, Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(HashMap<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the migrationMatrices
     */
    public TreeMap<String, MigrationMatrix> getMigrationMatrices() {
        return migrationMatrices;
    }

    /**
     * @param migrationMatrices the migrationMatrices to set
     */
    public void setMigrationMatrices(TreeMap<String, MigrationMatrix> migrationMatrices) {
        this.migrationMatrices = migrationMatrices;
    }

    /**
     * @return the transitionMatrices
     */
    public TreeMap<String, MigrationMatrix> getTransitionMatrices() {
        return transitionMatrices;
    }

    /**
     * @param transitionMatrices the transitionMatrices to set
     */
    public void setTransitionMatrices(TreeMap<String, MigrationMatrix> transitionMatrices) {
        this.transitionMatrices = transitionMatrices;
    }

    /**
     * @return the frames
     */
    public ArrayList<TimeFrame> getFrames() {
        return frames;
    }

    /**
     * @param frames the frames to set
     */
    public void setFrames(ArrayList<TimeFrame> frames) {
        this.frames = frames;
    }

    /**
     * @return the numStartPops
     */
    public int getNumStartPops() {
        return numStartPops;
    }

    /**
     * @param numStartPops the numStartPops to set
     */
    public void setNumStartPops(int numStartPops) {
        this.numStartPops = numStartPops;
    }

    public enum QuantityType {

        coalescent, expectedFraction, expectedNumberOfSegments;
    };
}
