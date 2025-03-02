import be.uclouvain.DicomImage;
import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.math3.linear.RealMatrix;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample DICOM files to be used in the context of this project:
 * "ct-brain.dcm", "hand.dcm", "mri-brain.dcm",
 * "us-palette-echocardiogram.dcm", and "us-rgb-liver.dcm".
 **/
public class App {
    /**
     * Given a DICOM instance, this method must send the content of
     * the pixel data to the JavaScript application. Two cases must be
     * considered:
     * 
     * - If the DICOM instance contains a grayscale image, the image
     *   must be returned using
     *   `DicomImage.sendImageToJavaScript()`. Furthermore, the
     *   floating-point numbers must be mapped into the range [0,
     *   255]: The minimum value in the pixel data must be set to 0.0,
     *   and the maximum value must be set to 255.0. This process is
     *   called "range normalization" (cf. Session 8 about the basics
     *   of image processing).
     * 
     * - If the DICOM instance contains a color image, the image must
     *   be returned using `HttpToolbox.sendImageToJavaScript()`.
     **/
    public static void renderDicom(HttpExchange exchange,
                                   DicomImage dicom) throws IOException {

        if (dicom.isGrayscale()) {

            RealMatrix image = dicom.getFloatPixelData();

            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (int i=0; i<image.getRowDimension(); i++) {

                for (int j=0; j<image.getColumnDimension(); j++) {

                    if (image.getEntry(i, j) < min) {
                        min = image.getEntry(i, j);
                    } else if (image.getEntry(i, j) > max) {
                        max = image.getEntry(i, j);
                    }

                }

            }

            RealMatrix normalizedMatrix = image.scalarAdd(-min).scalarMultiply(255.0 / (max - min));

            DicomImage.sendImageToJavaScript(exchange, normalizedMatrix);

        } else {
            HttpToolbox.sendImageToJavaScript(exchange, dicom.getColorPixelData());
        }

    }

    /**
     * Given a DICOM instance, this method must answer with a JSON
     * object that contains two fields: The field "patient-name" must
     * contain the string value of the Patient Name tag
     * (0x0010,0x0010) in the dataset, and the field
     * "study-description" must contain the string value of the Study
     * Description tag (0x0008,0x1030). If either of those tags is not
     * set, the corresponding field must be set to the string "None".
     **/
    public static void parseTags(HttpExchange exchange,
                                 DicomImage dicom) throws IOException {

        JSONObject response = new JSONObject();

        Attributes dataset = dicom.getDataset();

        String patientName = dataset.getString(Tag.PatientName, "None");

        response.put("patient-name", patientName);

        String description = dataset.getString(Tag.StudyDescription, "None");

        response.put("study-description", description);

        HttpToolbox.sendResponse(exchange, response);
    }
}
