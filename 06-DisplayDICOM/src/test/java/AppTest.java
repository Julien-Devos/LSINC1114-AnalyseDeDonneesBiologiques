import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.uclouvain.HttpToolbox;
import be.uclouvain.MockHttpExchange;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import org.json.JSONObject;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    static private void testTags(String resource,
                                 String patientName,
                                 String studyDescription) throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();

        final byte[] dicom = HttpToolbox.readResource(resource);
        byte[] answer = MockHttpExchange.executeMultipartUploadAsBytes(app, "/parse-tags", "data", dicom);
        JSONObject json = HttpToolbox.parseJsonObject(answer);

        assertEquals(2, json.length());
        assertEquals(patientName, json.getString("patient-name"));
        assertEquals(studyDescription, json.getString("study-description"));
    }

    static private BufferedImage getImage(String resource) throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();

        final byte[] dicom = HttpToolbox.readResource(resource);
        byte[] answer = MockHttpExchange.executeMultipartUploadAsBytes(app, "/render-dicom", "data", dicom);
        return MockHttpExchange.decodeRawImage(answer);
    }

    static private int getMinValue(BufferedImage image) {
        if (image.getWidth() == 0 ||
                image.getHeight() == 0) {
            return 0;
        } else {
            int minValue = MockHttpExchange.getRed(image, 0, 0);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int v = MockHttpExchange.getRed(image, x, y);
                    if (v < minValue) {
                        minValue = v;
                    }
                }
            }
            return minValue;
        }
    }

    static private int getMaxValue(BufferedImage image) {
        if (image.getWidth() == 0 ||
                image.getHeight() == 0) {
            return 0;
        } else {
            int maxValue = MockHttpExchange.getRed(image, 0, 0);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int v = MockHttpExchange.getRed(image, x, y);
                    if (v > maxValue) {
                        maxValue = v;
                    }
                }
            }
            return maxValue;
        }
    }


    @Test
    @Grade(value = 1)
    public void testHand() throws IOException, URISyntaxException {
        testTags("/hand.dcm", "Jodogne", "None");
        BufferedImage image = getImage("/hand.dcm");
        assertTrue(MockHttpExchange.isGrayscale(image));
        assertEquals(338, image.getWidth());
        assertEquals(337, image.getHeight());
        assertEquals(0, getMinValue(image));
        assertEquals(255, getMaxValue(image));
        assertEquals(52, MockHttpExchange.getRed(image, 0,0));
        assertEquals(175, MockHttpExchange.getRed(image, 185,200));
    }

    @Test
    @Grade(value = 1)
    public void testCtBrain() throws IOException, URISyntaxException {
        testTags("/ct-brain.dcm", "PHENIX", "CT2 tête, face, sinus");
        BufferedImage image = getImage("/ct-brain.dcm");
        assertTrue(MockHttpExchange.isGrayscale(image));
        assertEquals(512, image.getWidth());
        assertEquals(512, image.getHeight());
        assertEquals(0, getMinValue(image));
        assertEquals(255, getMaxValue(image));
        assertEquals(0, MockHttpExchange.getRed(image, 0,0));
        assertEquals(91, MockHttpExchange.getRed(image, 185,200));
        assertEquals(230, MockHttpExchange.getRed(image, 385,400));
    }

    @Test
    @Grade(value = 1)
    public void testMriBrain() throws IOException, URISyntaxException {
        testTags("/mri-brain.dcm", "BRAINIX", "IRM cérébrale, neuro-crâne");
        BufferedImage image = getImage("/mri-brain.dcm");
        assertTrue(MockHttpExchange.isGrayscale(image));
        assertEquals(512, image.getWidth());
        assertEquals(512, image.getHeight());
        assertEquals(0, getMinValue(image));
        assertEquals(255, getMaxValue(image));
        assertEquals(0, MockHttpExchange.getRed(image, 0,0));
        assertEquals(31, MockHttpExchange.getRed(image, 385,400));
    }

    @Test
    @Grade(value = 1)
    public void testRgb() throws IOException, URISyntaxException {
        testTags("/us-rgb-liver.dcm", "LiverUS-01", "None");
        BufferedImage image = getImage("/us-rgb-liver.dcm");
        assertFalse(MockHttpExchange.isGrayscale(image));
        assertEquals(720, image.getHeight());
        assertEquals(1280, image.getWidth());
        assertTrue(MockHttpExchange.testPixel(image, 64, 100, 255, 255, 0));
        assertTrue(MockHttpExchange.testPixel(image, 977, 496, 36, 25, 22));
    }

    @Test
    @Grade(value = 1)
    public void testPalette() throws IOException, URISyntaxException {
        testTags("/us-palette-echocardiogram.dcm", "Roberta Johnson", "Echocardiogram");
        BufferedImage image = getImage("/us-palette-echocardiogram.dcm");
        assertFalse(MockHttpExchange.isGrayscale(image));
        assertEquals(430, image.getHeight());
        assertEquals(600, image.getWidth());
        assertTrue(MockHttpExchange.testPixel(image, 369, 171, 6, 147, 44));
        assertTrue(MockHttpExchange.testPixel(image, 58, 192, 0, 0, 0));
    }
}
