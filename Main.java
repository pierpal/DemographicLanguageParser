package demographicLanguageParser;

/**
 * Main class to create/test demographic model.
 *
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Main {

    /**
     * Main function
     *
     */
    public static void main(String[] args) throws Exception {
        DemographicLanguageParser model = new DemographicLanguageParser(args[0]);
        model.checkConstraints();

        System.out.println("Start: ");
        for (int iter = 0; iter < 1; iter++) {
            model.buildMigrationMatrices();
            Edge[] popsAt0 = model.getPopsAt0();
            model.narrate();
            double[] funArgs;
            Matrix res;

            if (false) {
                for (double u = 1.0; u < 20.; u++) {
                    funArgs = new double[]{u, u + 1.0, 100.};
                    res = model.computeModelQuantity(
                            DemographicLanguageParser.QuantityType.expectedNumberOfSegments, funArgs);
                }
                if (iter % 1 == 0) {
                    System.out.println(iter);
                }
            }
        }
    }
}
