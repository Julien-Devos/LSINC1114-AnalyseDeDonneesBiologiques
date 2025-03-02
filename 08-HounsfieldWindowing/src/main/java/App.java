import be.uclouvain.DicomImage;
import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.math3.linear.MatrixUtils;
import org.dcm4che3.data.Tag;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample DICOM file to be used in the context of this project:
 * "ct-brain.dcm".
 **/
public class App {

    /**
     * This method must extract the matrix of pixel data from a
     * grayscale DICOM image, then use the rescale slope/intercept
     * DICOM tags to recover the actual Hounsfield floating-point
     * value of each pixel (cf. theoretical slides).
     *
     * @param image The DICOM image of interest.
     * @return The matrix of the actual Hounsfield values.
     */
    public static RealMatrix applyRescale(DicomImage image) {
        double rescaleSlope = image.getDataset().getDouble(Tag.RescaleSlope, 1.0);
        double rescaleIntercept = image.getDataset().getDouble(Tag.RescaleIntercept,0.0);

        RealMatrix pixelData = image.getFloatPixelData();

        for (int i=0; i<pixelData.getRowDimension(); i++) {
            for (int j=0; j<pixelData.getColumnDimension(); j++) {
                double p = pixelData.getEntry(i, j);

                pixelData.setEntry(i, j, rescaleSlope * p + rescaleIntercept);

            }
        }

        return pixelData;
    }

    /**
     * This GET route in the REST API must compute the minimal and
     * maximal values in the pixel data, expressed in Hounsfield
     * units, of the DICOM image provided as an argument, then it must
     * send its response with a body that contains a JSON dictionary
     * formatted as follows:
     *
     * {
     *   "low" : -1000,
     *   "high" : 3000
     * }
     *
     * @param exchange The context of this REST API call.
     * @param image    The DICOM image of interest.
     * @throws IOException If some error occurred during the HTTP transfer.
     */
    public static void getHounsfieldRange(HttpExchange exchange,
                                          DicomImage image) throws IOException {
        RealMatrix matrix = applyRescale(image);

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

        JSONObject response = new JSONObject();

        response.put("low", min);
        response.put("high", max);

        HttpToolbox.sendResponse(exchange, response);
    }

    /**
     * This POST route in the REST API must apply Hounsfield windowing
     * to the provided DICOM image. The resulting image must be sent
     * to the JavaScript front-end using
     * "DicomImage.sendImageToJavaScript()". The body of the POST
     * request shall contain a JSON dictionary formatted as follows:
     *
     * {
     *   "low" : 200,
     *   "high" : 1000
     * }
     *
     * HTTP status 400 must be sent if the body of the request doesn't
     * contain a valid JSON dictionary. If "high <= low", the returned
     * image must be entirely black.
     *
     * @param exchange The context of this REST API call.
     * @param image    The DICOM image of interest.
     * @throws IOException If some error occurred during the HTTP transfer.
     */
    public static void applyHounsfieldWindowing(HttpExchange exchange,
                                                DicomImage image) throws IOException {

        try {

            JSONObject dictionary = HttpToolbox.getRequestBodyAsJsonObject(exchange);

            double low = dictionary.getDouble("low");
            double high = dictionary.getDouble("high");

            RealMatrix pixelData = applyRescale(image);

            if (high <= low) {
                for (int i=0; i<pixelData.getRowDimension(); i++) {
                    for (int j=0; j<pixelData.getColumnDimension(); j++) {
                        pixelData.setEntry(i, j, 0);
                    }
                }
                DicomImage.sendImageToJavaScript(exchange, pixelData);
                return;
            }

            for (int i=0; i<pixelData.getRowDimension(); i++) {
                for (int j=0; j<pixelData.getColumnDimension(); j++) {
                    double current = pixelData.getEntry(i, j);

                    pixelData.setEntry(i, j, ( (current - low) / (high - low) ) * 255);

                }
            }

            DicomImage.sendImageToJavaScript(exchange, pixelData);

        } catch (Exception e) {

            HttpToolbox.sendBadRequest(exchange);

        }

    }
}
