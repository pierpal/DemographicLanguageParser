package demographicLanguageParser;

/**
 *
 * @author Pier Palamara <pier@cs.columbia.edu>
 */
public class Matrix {

    private String id;
    private Double[][] mat;

    public Matrix(int N) {
        mat = getIdentity(N);
    }

    public Matrix(Double[][] D) {
        this.mat = D;
    }

    public Matrix(double[][] d) {
        Double[][] D = new Double[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j<d.length; j++)  {
                D[i][j] = d[i][j];
            }
        }
        this.mat = D;
    }

    public Matrix(String id, int N) {
        this.id = id;
        mat = getIdentity(N);
    }

    public void printMat() {
        if (getId() != null) {
            System.out.println(getId());
        }
        for (int k = 0; k < getMat().length; k++) {
            for (int j = 0; j < getMat()[0].length; j++) {
                System.out.print(getMat()[k][j] + "\t");
            }
            System.out.println("");
        }
    }

    public static void printMat(double[][] mat) {
        for (int k = 0; k < mat.length; k++) {
            for (int j = 0; j < mat[0].length; j++) {
                System.out.print(mat[k][j] + "\t");
            }
            System.out.println("");
        }
    }

    public void printRowSum() {
        if (getId() != null) {
            System.out.println(getId());
        }
        for (int k = 0; k < getMat().length; k++) {
            double tot = 0;
            for (int j = 0; j < getMat()[0].length; j++) {
                tot += getMat()[k][j];
            }
            System.out.println(k + " -> " + tot);
        }
    }

    public void multiplyMatrices(Matrix mat2) throws Exception {
        Double[][] B = mat2.getMat();
        if (getMat()[0].length != B.length) {
            throw new Exception("inconsistent size of matrices in multiplication: " + getMat().length + "x" + getMat()[0].length + " and " + B.length + "x" + B[0].length);
        }
        Double[][] res = new Double[getMat().length][B[0].length];
        for (int i = 0; i < getMat().length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                res[i][j] = 0.0;
                for (int k = 0; k < getMat()[0].length; k++) {
                    res[i][j] += getMat()[i][k] * B[k][j];
                }
            }
        }
        setMat(res);
    }

    public void sumToMatrix(Matrix mat2) throws Exception {
//        printMat(A);
//        printMat(B);
        Double[][] B = mat2.getMat();
        if (getMat().length != B.length || getMat()[0].length != B[0].length) {
            throw new Exception("inconsistent size of matrices in summation: " + getMat().length + "x" + getMat()[0].length + " and " + B.length + "x" + B[0].length);
        }
        for (int i = 0; i < getMat().length; i++) {
            for (int j = 0; j < getMat()[0].length; j++) {
                getMat()[i][j] += mat2.getMat()[i][j];
            }
        }
    }

    // multiply M*N*M', where N is diagonal with elements from array diag
    public static Matrix multiplyDiagSelf(Matrix matrix, Double[] diag) throws Exception {
        Double[][] mat = matrix.getMat();
        Double[][] res = new Double[mat.length][mat.length];
        if (diag.length != mat[0].length) {
            throw new Exception("size of diagonal matrix does not match #columns of matrix: " + diag.length + " and " + mat[0].length);
        }
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                res[i][j] = 0.0;
                for (int k = 0; k < mat[0].length; k++) {
                    res[i][j] += mat[i][k] / diag[k] * mat[j][k];
                }
            }
        }
        return new Matrix(res);
    }

    public static Double[][] getIdentity(int N) {
        Double[][] ID = new Double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                ID[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }
        return ID;
    }

    public static Double[][] getZeroes(int N) {
        Double[][] ID = new Double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                ID[i][j] = 0.0;
            }
        }
        return ID;
    }

    public void sumScalar(double v) {
        for (int i = 0; i < getMat().length; i++) {
            for (int j = 0; j < getMat()[0].length; j++) {
                getMat()[i][j] += v;
            }
        }
    }

    public Matrix getTranspose() {
        Matrix res = new Matrix(new Double[getMat()[0].length][getMat().length]);
        for (int i = 0; i < getMat()[0].length; i++) {
            for (int j = 0; j < getMat().length; j++) {
                res.getMat()[i][j] = getMat()[j][i];
            }
        }
        return res;
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
     * @return the mat
     */
    public Double[][] getMat() {
        return mat;
    }

    /**
     * @param mat the mat to set
     */
    public void setMat(Double[][] mat) {
        this.mat = mat;
    }
}
