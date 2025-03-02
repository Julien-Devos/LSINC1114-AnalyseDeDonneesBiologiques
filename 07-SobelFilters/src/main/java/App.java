import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample DICOM files to be used in the context of this project:
 * "ct-brain.dcm", and "hand.dcm".
 **/
public class App {
    /**
     * Given a 2D matrix containing floating-point values (i.e. a
     * graylevel image), create a new matrix of the same size where
     * range normalization has been applied. This means that the
     * minimum value in the source matrix must be mapped to 0.0, and
     * the maximum value must be mapped to 255.0.
     * @param matrix The matrix of interest.
     * @return The normalized matrix.
     */
    public static RealMatrix rangeNormalization(RealMatrix matrix) {

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i=0; i<matrix.getRowDimension(); i++) {

            for (int j=0; j<matrix.getColumnDimension(); j++) {

                if (matrix.getEntry(i, j) < min) {
                    min = matrix.getEntry(i, j);
                } else if (matrix.getEntry(i, j) > max) {
                    max = matrix.getEntry(i, j);
                }

            }

        }

        RealMatrix normalizedMatrix = matrix.scalarAdd(-min).scalarMultiply(255.0 / (max - min));

        return normalizedMatrix;
    }

    /**
     * Compute the convolution of a 2D matrix (i.e. a graylevel image)
     * by a 2D convolution kernel. You must use the "valid"
     * convolution mode, which implies that no padding is required. If
     * the image is too small for the input kernel, an
     * "IllegalArgumentException()" must be thrown.
     * @param image The source 2D matrix.
     * @param kernel The convolution kernel.
     * @return The result of the convolution.
     */
    public static RealMatrix convolve(RealMatrix image,
                                      RealMatrix kernel) {

        if (image.getRowDimension() < kernel.getRowDimension() || image.getColumnDimension() < kernel.getColumnDimension()) {
            throw new IllegalArgumentException();
        }

        RealMatrix computedMatrix = MatrixUtils.createRealMatrix(image.getRowDimension() - kernel.getRowDimension() + 1,
                image.getColumnDimension() - kernel.getColumnDimension() + 1);

        for (int i=0; i<computedMatrix.getRowDimension(); i++) {

            for (int j=0; j<computedMatrix.getColumnDimension(); j++) {
                double sum = 0.0;

                for (int k=0; k<kernel.getRowDimension(); k++) {

                    for (int l=0; l<kernel.getColumnDimension(); l++) {
                        sum += image.getEntry(i + k, j + l) * kernel.getEntry(k, l);
                    }

                }

                computedMatrix.setEntry(i, j, sum);
            }

        }

        return computedMatrix;
    }

    /**
     * Compute the convolution of a 2D matrix (i.e. a graylevel image)
     * with the horizontal Sobel kernel (i.e. "dI/dx" in the
     * slides). Evidently, make sure to use the "convolve()" method
     * above.
     * @param image The source 2D matrix.
     * @return The result of the convolution.
     */
    public static RealMatrix sobelX(RealMatrix image) {

        RealMatrix sobelKernelX = MatrixUtils.createRealMatrix(new double[][] {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1} });

        return convolve(image, sobelKernelX);
    }

    /**
     * Compute the convolution of a 2D matrix (i.e. a graylevel image)
     * with the vertical Sobel kernel (i.e. "dI/dy" in the
     * slides). Evidently, make sure to use the "convolve()" method
     * defined above.
     * @param image The source 2D matrix.
     * @return The result of the convolution.
     */
    public static RealMatrix sobelY(RealMatrix image) {

        RealMatrix sobelKernelY = MatrixUtils.createRealMatrix(new double[][] {
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1} });

        return convolve(image, sobelKernelY);
    }

    /**
     * Compute the approximate magnitude of the gradient of a 2D
     * matrix (i.e. a graylevel image), as obtained through the Sobel
     * kernels. By "approximate", we mean that you have to use the
     * formula using the absolute values from the slides (*not* the
     * formula with the square root). Evidently, make sure to use the
     * "sobelX()" and "sobelY()" methods defined above.
     * @param image The source 2D matrix.
     * @return 2D matrix containing the magnitude of the gradient.
     */
    public static RealMatrix sobelMagnitude(RealMatrix image) {
        RealMatrix sobel1 = sobelX(image);
        RealMatrix sobel2 = sobelY(image);

        RealMatrix computedMatrix = MatrixUtils.createRealMatrix(sobel1.getRowDimension(), sobel1.getColumnDimension());

        for (int i=0; i<sobel1.getRowDimension(); i++) {

            for (int j=0; j<sobel1.getColumnDimension(); j++) {

                computedMatrix.setEntry(i, j, Math.abs(sobel1.getEntry(i, j)) + Math.abs(sobel2.getEntry(i, j)));

            }

        }

        return computedMatrix;
    }
}
