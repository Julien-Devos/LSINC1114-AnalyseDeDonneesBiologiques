import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import be.uclouvain.ComplexNumber;
import be.uclouvain.HttpToolbox;
import be.uclouvain.MockHttpExchange;
import be.uclouvain.Signal;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    static Signal getTestSource() {
        Signal source = new Signal(32);
        source.setValue(0,0.70509118309693,0);
        source.setValue(1,-0.405508613135026,0);
        source.setValue(2,-2.26351161945286,0);
        source.setValue(3,-2.09329070886447,0);
        source.setValue(4,0.442515291446171,0);
        source.setValue(5,-0.910068866573073,0);
        source.setValue(6,-0.150132604606928,0);
        source.setValue(7,2.1508065128513,0);
        source.setValue(8,1.38891923208738,0);
        source.setValue(9,-0.467779212724641,0);
        source.setValue(10,-1.04963556598103,0);
        source.setValue(11,-3.40334218422218,0);
        source.setValue(12,0.878413958615826,0);
        source.setValue(13,-1.58105679350092,0);
        source.setValue(14,1.02844638866831,0);
        source.setValue(15,-0.781410122589297,0);
        source.setValue(16,-1.32822838820417,0);
        source.setValue(17,-1.48079757201235,0);
        source.setValue(18,-1.24284979376391,0);
        source.setValue(19,0.402748579986504,0);
        source.setValue(20,0.120438754080217,0);
        source.setValue(21,-0.754766032330705,0);
        source.setValue(22,-0.274044544730586,0);
        source.setValue(23,1.05064481296011,0);
        source.setValue(24,0.380542461871342,0);
        source.setValue(25,1.11649888089076,0);
        source.setValue(26,1.38371045284751,0);
        source.setValue(27,0.137947752355161,0);
        source.setValue(28,-0.326430018362721,0);
        source.setValue(29,0.113105351374796,0);
        source.setValue(30,0.442226847534526,0);
        source.setValue(31,0.474191769805855,0);
        return source;
    }

    static Signal getTestFourier() {
        Signal fourier = new Signal(32);
        fourier.setValue(0,-6.29660441058215,0);
        fourier.setValue(1,4.92739300206677,6.12336740753445);
        fourier.setValue(2,-8.67396857348882,2.39881542776743);
        fourier.setValue(3,-3.0378125189732,3.97477688532559);
        fourier.setValue(4,6.92240326115523,8.42958207672214);
        fourier.setValue(5,10.6091018799295,-2.80030139314352);
        fourier.setValue(6,-4.96655446129868,3.69422645835354);
        fourier.setValue(7,2.99777294747741,3.04005895526775);
        fourier.setValue(8,4.38705291411594,2.30866927029415);
        fourier.setValue(9,0.832333615216302,-5.73587176845124);
        fourier.setValue(10,2.93283065027539,-4.43863396709975);
        fourier.setValue(11,0.661009277167754,-1.97811990215969);
        fourier.setValue(12,-6.85963025501124,-0.00798314970905345);
        fourier.setValue(13,2.39782301812338,5.08223469554866);
        fourier.setValue(14,1.13729678824826,-5.69016457659273);
        fourier.setValue(15,-3.1210646505991,5.69972716478301);
        fourier.setValue(16,6.56754848087419,0);
        fourier.setValue(17,-3.1210646505991,-5.69972716478301);
        fourier.setValue(18,1.13729678824826,5.69016457659273);
        fourier.setValue(19,2.39782301812338,-5.08223469554866);
        fourier.setValue(20,-6.85963025501124,0.00798314970905345);
        fourier.setValue(21,0.661009277167754,1.97811990215969);
        fourier.setValue(22,2.93283065027539,4.43863396709975);
        fourier.setValue(23,0.832333615216302,5.73587176845124);
        fourier.setValue(24,4.38705291411594,-2.30866927029415);
        fourier.setValue(25,2.99777294747741,-3.04005895526775);
        fourier.setValue(26,-4.96655446129868,-3.69422645835354);
        fourier.setValue(27,10.6091018799295,2.80030139314352);
        fourier.setValue(28,6.92240326115523,-8.42958207672214);
        fourier.setValue(29,-3.0378125189732,-3.97477688532559);
        fourier.setValue(30,-8.67396857348882,-2.39881542776743);
        fourier.setValue(31,4.92739300206677,-6.12336740753445);
        return fourier;
    }

    static double[] getTestPowerSpectrum(double samplingFrequency) {
        double[] rawSpectrum = new double[32];
        rawSpectrum[0] = 39.6472271033626;
        rawSpectrum[1] = 61.7748302044718;
        rawSpectrum[2] = 80.9920462703666;
        rawSpectrum[3] = 25.0271561885489;
        rawSpectrum[4] = 118.977520898248;
        rawSpectrum[5] = 120.394730591164;
        rawSpectrum[6] = 38.3139723426452;
        rawSpectrum[7] = 18.228601096131;
        rawSpectrum[8] = 24.5761870708537;
        rawSpectrum[9] = 33.593004191135;
        rawSpectrum[10] = 28.3029671170865;
        rawSpectrum[11] = 4.34989161182208;
        rawSpectrum[12] = 47.0545909661449;
        rawSpectrum[13] = 31.578664726881;
        rawSpectrum[14] = 33.6714168932706;
        rawSpectrum[15] = 42.2279343061847;
        rawSpectrum[16] = 43.1326930486329;
        rawSpectrum[17] = 42.2279343061847;
        rawSpectrum[18] = 33.6714168932706;
        rawSpectrum[19] = 31.578664726881;
        rawSpectrum[20] = 47.0545909661449;
        rawSpectrum[21] = 4.34989161182208;
        rawSpectrum[22] = 28.3029671170865;
        rawSpectrum[23] = 33.593004191135;
        rawSpectrum[24] = 24.5761870708537;
        rawSpectrum[25] = 18.228601096131;
        rawSpectrum[26] = 38.3139723426452;
        rawSpectrum[27] = 120.394730591164;
        rawSpectrum[28] = 118.977520898248;
        rawSpectrum[29] = 25.0271561885489;
        rawSpectrum[30] = 80.9920462703666;
        rawSpectrum[31] = 61.7748302044718;

        double[] powerSpectrum = new double[rawSpectrum.length];
        for (int i = 0; i < rawSpectrum.length; i++) {
            powerSpectrum[i] = rawSpectrum[i] / (rawSpectrum.length * samplingFrequency);
        }
        
        return powerSpectrum;
    }

    public static void compareSignals(Signal s1,
                                      Signal s2) {
        System.out.println("S1: "+s1.getLength()+"S2 "+s2.getLength());
        assertEquals (s1.getLength(), s2.getLength());
        
        for (int i = 0; i < s1.getLength(); i++) {
            ComplexNumber a = s1.getValue(i);
            ComplexNumber b = s2.getValue(i);
            System.out.println("a: "+a.getReal()+" "+a.getImag()+" b: "+b.getReal()+" "+b.getImag());
            ComplexNumber c = new ComplexNumber(a.getReal() - b.getReal(), a.getImag() - b.getImag());
            assertTrue(c.getModulus() <= 0.0000001);
        }
    }

    @Test
    @Grade(value = 1)
    public void testDFT() {
        compareSignals(getTestFourier(), App.computeDFT(getTestSource()));
    }

    @Test
    @Grade(value = 1)
    public void testFFT() {
        compareSignals(getTestFourier(), App.computeFFT(getTestSource()));
    }

    @Test
    @Grade(value = 1)
    public void testEdgeCases() {
        compareSignals(new Signal(0), App.computeDFT(new Signal(0)));
        compareSignals(new Signal(0), App.computeFFT(new Signal(0)));

        Signal a = new Signal(1);
        a.setValue(0, 42);

        compareSignals(a, App.computeDFT(a));
        compareSignals(a, App.computeFFT(a));
        
        a = new Signal(2);
        a.setValue(0, 5);
        a.setValue(1, 13);

        Signal b = new Signal(2);
        b.setValue(0, 18);
        b.setValue(1, -8);

        compareSignals(b, App.computeDFT(a));
        compareSignals(b, App.computeFFT(a));
        
        a = new Signal(3);
        a.setValue(0, 5);
        a.setValue(1, 13);
        a.setValue(2, 7);

        b = new Signal(3);
        b.setValue(0, 25);
        b.setValue(1, -5, -5.196152422706632);
        b.setValue(2, -5, 5.196152422706632);

        compareSignals(b, App.computeDFT(a));

        try {
            App.computeFFT(a);  // Must be a power of 2
            fail();
        } catch (IllegalArgumentException e) {
        }        
    }

    @Test
    @Grade(value = 1)
    public void testPowerSpectrum() {
        final double samplingFrequency = getTestSource().getLength(); 
        final double[] s1 = App.computePowerSpectrum(App.computeDFT(getTestSource()), samplingFrequency);
        final double[] s2 = getTestPowerSpectrum(samplingFrequency);
        assertEquals (s1.length, s2.length);
        for (int i = 0; i < s2.length; i++) {
            assertTrue(Math.abs(s1[i] - s2[i]) <= 0.0000001);
        }
    }

    @Test
    @Grade(value = 1)
    public void testComplexityDFT() {
        final int N = 256;
        
        Signal.resetCounters();
        App.computeDFT(new Signal(N));
        int dft1 = Signal.getCountReads();
        Signal.resetCounters();
        App.computeDFT(new Signal(N * 2));
        int dft2 = Signal.getCountReads();
        Signal.resetCounters();
        App.computeDFT(new Signal(N * 8));
        int dft3 = Signal.getCountReads();

        // This checks N^2 complexity, with some tolerance
        assertEquals(1.0, (double) (dft1 * 2 * 2) / (double) dft2, 0.1);
        assertEquals(1.0, (double) (dft1 * 8 * 8) / (double) dft3, 0.1);
    }

    @Test
    @Grade(value = 2)
    public void testComplexityFFT() {
        final int N = 256;
        
        Signal.resetCounters();
        App.computeFFT(new Signal(N));
        int fft1 = Signal.getCountReads();
        Signal.resetCounters();
        App.computeFFT(new Signal(N * 2));
        int fft2 = Signal.getCountReads();
        Signal.resetCounters();
        App.computeFFT(new Signal(N * 8));
        int fft3 = Signal.getCountReads();

        // This checks N*log(N) complexity, with some tolerance
        assertEquals(1.0, (double) (fft1 * 2) * Math.log(2 * N) / ((double) fft2 * Math.log(N)), 0.01);
        assertEquals(1.0, (double) (fft1 * 8) * Math.log(8 * N) / ((double) fft3 * Math.log(N)), 0.01);
    }

    @Test
    @Grade(value = 1)
    public void testDC() {
        final int N = 16;
        
        Signal zeros = new Signal(N);
        compareSignals(zeros, App.computeDFT(zeros));
        compareSignals(zeros, App.computeFFT(zeros));

        Signal dc = new Signal(N);
        for (int i = 0; i < N; i++) {
            dc.setValue(i, 42);
        }

        Signal expected = new Signal(N);
        expected.setValue(0, 42 * N);

        compareSignals(expected, App.computeDFT(dc));
        compareSignals(expected, App.computeFFT(dc));        
    }

    @Test
    @Grade(value = 1)
    public void testHarmonic() {
        final int N = 16;

        for (int k = 1; k < N / 2; k++) {
            Signal sine = new Signal(N);
            Signal cosine = new Signal(N);
            for (int i = 0; i < N; i++) {
                sine.setValue(i, Math.sin(2.0 * Math.PI * (double) (k*i) / (double) N));
                cosine.setValue(i, Math.cos(2.0 * Math.PI * (double) (k*i) / (double) N));
            }

            Signal pulse = new Signal(N);
            pulse.setValue(k, 0, -N / 2);
            pulse.setValue(N - k, 0, N / 2);
            compareSignals(pulse, App.computeDFT(sine));
            compareSignals(pulse, App.computeFFT(sine));

            pulse.setValue(k, N / 2, 0);
            pulse.setValue(N - k, N / 2, 0);
            compareSignals(pulse, App.computeDFT(cosine));
            compareSignals(pulse, App.computeFFT(cosine));            
        }
    }

    static void testSinglePeak(JSONArray a,
                                double frequency) {
        // Expected frequencies are from 0Hz to 100Hz, as the sampling frequency is 200Hz for channel 6 ("sine 8 Hz")
        for (int i = 0; i < a.length(); i++) {
            assertEquals((double) i / (double) (a.length() - 1) * 100.0, a.getJSONObject(i).getDouble("x"), 0.000001);
        }

        int countPeaks = 0;
        for (int i = 0; i < a.length(); i++) {
            double x = a.getJSONObject(i).getDouble("x");
            double y = a.getJSONObject(i).getDouble("y");
            if (Math.abs(frequency - x) >= 1) {
                assertEquals(0, y, 1);
            } else if (Math.abs(frequency - x) <= 0.01) {
                assertTrue(y > 1);
                countPeaks++;
            }
        }

        assertTrue(countPeaks > 3);
    }

    @Test
    @Grade(value = 2)
    public void testRestApi() throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();
        assertEquals(405, MockHttpExchange.executeGetAsStatusCode(app, "/upload"));
        assertEquals(405, MockHttpExchange.executeGetAsStatusCode(app, "/compute-power-spectrum"));
        assertEquals(400, MockHttpExchange.executePostAsStatusCode(app, "/compute-power-spectrum", MockHttpExchange.stringToBytes("nope")));
        assertEquals(400, MockHttpExchange.executePostAsStatusCode(app, "/compute-power-spectrum", MockHttpExchange.stringToBytes("{}")));
        assertEquals(404, MockHttpExchange.executePostAsStatusCode(app, "/compute-power-spectrum", MockHttpExchange.stringToBytes("{\"channel\":6}")));

        final byte[] edf = HttpToolbox.readResource("/test_generator_2.edf");
        final byte[] tmp = MockHttpExchange.executeMultipartUploadAsBytes(app, "/upload", "data", edf);
        final JSONObject response = HttpToolbox.parseJsonObject(tmp);
        assertEquals(response.getInt("sine 8 Hz"), 6);
        assertEquals(response.getInt("sine 1 Hz"), 5);

        assertEquals(404, MockHttpExchange.executePostAsStatusCode(app, "/compute-power-spectrum", MockHttpExchange.stringToBytes("{\"channel\":66}")));
        JSONArray a = HttpToolbox.parseJsonArray(MockHttpExchange.executePostAsBytes(
                app, "/compute-power-spectrum", MockHttpExchange.stringToBytes("{\"channel\":6}")));
        assertEquals(65537, a.length());
        testSinglePeak(a, 8.0);

        a = HttpToolbox.parseJsonArray(MockHttpExchange.executePostAsBytes(
                app, "/compute-power-spectrum", MockHttpExchange.stringToBytes("{\"channel\":5}")));
        assertEquals(65537, a.length());
        testSinglePeak(a, 1.0);
    }
}
