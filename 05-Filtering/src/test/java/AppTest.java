import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import be.uclouvain.ComplexNumber;
import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;
import be.uclouvain.Signal;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    public static void compareSignals(Signal s1,
                                      Signal s2) {
        assertEquals (s1.getLength(), s2.getLength());
        
        for (int i = 0; i < s1.getLength(); i++) {
            ComplexNumber a = s1.getValue(i);
            ComplexNumber b = s2.getValue(i);
            ComplexNumber c = new ComplexNumber(a.getReal() - b.getReal(), a.getImag() - b.getImag());
            assertTrue(c.getModulus() <= 0.0000001);
        }
    }

    @Test
    @Grade(value = 1)
    public void testIFFT() {
        try {
            Signal s = new Signal(3);
            App.computeFFT(s);
            fail();
        } catch (IllegalArgumentException e) {
        }
        
        try {
            Signal s = new Signal(3);
            App.computeInverseFFT(s);
            fail();
        } catch (IllegalArgumentException e) {
        }
        
        for (int l = 1; l <= 256; l *= 2) {
            Signal s = new Signal(l);
            System.out.println("l: "+l);
            compareSignals(s, App.computeInverseFFT(App.computeFFT(s)));
            
            for (int i = 0; i < s.getLength(); i++) {
                s.setValue(i, i);
            }

            System.out.println("FFT: "+App.computeFFT(s).getLength()+" IFFT: "+App.computeInverseFFT(App.computeFFT(s)).getLength()+" S: "+s.getLength());
            compareSignals(s, App.computeInverseFFT(App.computeFFT(s)));
        }
    }

    @Test
    @Grade(value = 1)
    public void testFrequency() {
        assertEquals(0, App.getFrequency(0, 1, 200), 0.0001);

        assertEquals(0, App.getFrequency(0, 2, 200), 0.0001);
        assertEquals(100, App.getFrequency(1, 2, 200), 0.0001);

        assertEquals(0, App.getFrequency(0, 3, 200), 0.0001);
        assertEquals(200.0 / 3.0, App.getFrequency(1, 3, 200), 0.0001);
        assertEquals(-200.0 / 3.0, App.getFrequency(2, 3, 200), 0.0001);

        assertEquals(0, App.getFrequency(0, 4, 200), 0.0001);
        assertEquals(50, App.getFrequency(1, 4, 200), 0.0001);
        assertEquals(100, App.getFrequency(2, 4, 200), 0.0001);
        assertEquals(-50, App.getFrequency(3, 4, 200), 0.0001);

        assertEquals(0, App.getFrequency(0, 5, 200), 0.0001);
        assertEquals(40, App.getFrequency(1, 5, 200), 0.0001);
        assertEquals(80, App.getFrequency(2, 5, 200), 0.0001);
        assertEquals(-80, App.getFrequency(3, 5, 200), 0.0001);
        assertEquals(-40, App.getFrequency(4, 5, 200), 0.0001);

        assertEquals(0, App.getFrequency(0, 5, 100), 0.0001);
        assertEquals(20, App.getFrequency(1, 5, 100), 0.0001);
        assertEquals(40, App.getFrequency(2, 5, 100), 0.0001);
        assertEquals(-40, App.getFrequency(3, 5, 100), 0.0001);
        assertEquals(-20, App.getFrequency(4, 5, 100), 0.0001);

        assertEquals(0, App.getFrequency(0, 10, 200), 0.0001);
        assertEquals(20, App.getFrequency(1, 10, 200), 0.0001);
        assertEquals(40, App.getFrequency(2, 10, 200), 0.0001);
        assertEquals(60, App.getFrequency(3, 10, 200), 0.0001);
        assertEquals(80, App.getFrequency(4, 10, 200), 0.0001);
        assertEquals(100, App.getFrequency(5, 10, 200), 0.0001);
        assertEquals(-80, App.getFrequency(6, 10, 200), 0.0001);
        assertEquals(-60, App.getFrequency(7, 10, 200), 0.0001);
        assertEquals(-40, App.getFrequency(8, 10, 200), 0.0001);
        assertEquals(-20, App.getFrequency(9, 10, 200), 0.0001);

        try {
            App.getFrequency(10, 10, 200);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            App.getFrequency(0, 0, 200);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            App.getFrequency(-10, 10, 200);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    @Grade(value = 1)
    public void testComplexityIFFT() {
        final int N = 256;
        
        Signal.resetCounters();
        App.computeInverseFFT(new Signal(N));
        int fft1 = Signal.getCountReads();
        Signal.resetCounters();
        App.computeInverseFFT(new Signal(N * 2));
        int fft2 = Signal.getCountReads();
        Signal.resetCounters();
        App.computeInverseFFT(new Signal(N * 8));
        int fft3 = Signal.getCountReads();

        // This checks N*log(N) complexity, with some tolerance
        assertEquals(1.0, (double) (fft1 * 2) * Math.log(2 * N) / ((double) fft2 * Math.log(N)), 0.1);
        assertEquals(1.0, (double) (fft1 * 8) * Math.log(8 * N) / ((double) fft3 * Math.log(N)), 0.1);
    }

    static private void checkFilter(double Fs,
                                    boolean hasHighpass,
                                    double highpassCutoff,
                                    boolean hasLowpass,
                                    double lowpassCutoff,
                                    double[] expected) {
        final int N = expected.length;
        Signal filter = App.createFilter(N, Fs, hasHighpass, highpassCutoff, hasLowpass, lowpassCutoff);
        assertEquals(N, filter.getLength());

        for (int i = 0; i < N; i++) {
            assertEquals(0, filter.getValue(i).getImag(), 0.000001);
            assertEquals(expected[i], filter.getValue(i).getReal(), 0.000001);
        }
    }

    @Test
    @Grade(value = 1)
    public void testCreateFilter() {
        checkFilter(1, false, 0, false, 0, new double[] { });
        checkFilter(1, false, 0, false, 0, new double[] { 1 });
        checkFilter(1, false, 0, false, 0, new double[] { 1, 1, 1, 1, 1 });

        System.out.println();
        
        checkFilter(1, true, 1.0 / 5.0 - 0.001, false, 0, new double[] { 0, 1, 1, 1, 1 });
        checkFilter(1, true, 1.0 / 5.0 + 0.001, false, 0, new double[] { 0, 0, 1, 1, 0 });
        checkFilter(1, true, 2.0 / 5.0 - 0.001, false, 0, new double[] { 0, 0, 1, 1, 0 });
        checkFilter(1, true, 2.0 / 5.0 + 0.001, false, 0, new double[] { 0, 0, 0, 0, 0 });
        checkFilter(1, true, 1.0 / 6.0 - 0.001, false, 0, new double[] { 0, 1, 1, 1, 1, 1 });
        checkFilter(1, true, 1.0 / 6.0 + 0.001, false, 0, new double[] { 0, 0, 1, 1, 1, 0 });
        checkFilter(1, true, 2.0 / 6.0 - 0.001, false, 0, new double[] { 0, 0, 1, 1, 1, 0 });
        checkFilter(1, true, 2.0 / 6.0 + 0.001, false, 0, new double[] { 0, 0, 0, 1, 0, 0 });
        checkFilter(1, true, 3.0 / 6.0 - 0.001, false, 0, new double[] { 0, 0, 0, 1, 0, 0 });
        checkFilter(1, true, 3.0 / 6.0 + 0.001, false, 0, new double[] { 0, 0, 0, 0, 0, 0 });
        
        checkFilter(1, false, 0, true, 1.0 / 5.0 - 0.001, new double[] { 1, 0, 0, 0, 0 });
        checkFilter(1, false, 0, true, 1.0 / 5.0 + 0.001, new double[] { 1, 1, 0, 0, 1 });
        checkFilter(1, false, 0, true, 2.0 / 5.0 - 0.001, new double[] { 1, 1, 0, 0, 1 });
        checkFilter(1, false, 0, true, 2.0 / 5.0 + 0.001, new double[] { 1, 1, 1, 1, 1 });
        checkFilter(1, false, 0, true, 1.0 / 6.0 - 0.001, new double[] { 1, 0, 0, 0, 0, 0 });
        checkFilter(1, false, 0, true, 1.0 / 6.0 + 0.001, new double[] { 1, 1, 0, 0, 0, 1 });
        checkFilter(1, false, 0, true, 2.0 / 6.0 - 0.001, new double[] { 1, 1, 0, 0, 0, 1 });
        checkFilter(1, false, 0, true, 2.0 / 6.0 + 0.001, new double[] { 1, 1, 1, 0, 1, 1 });
        checkFilter(1, false, 0, true, 3.0 / 6.0 - 0.001, new double[] { 1, 1, 1, 0, 1, 1 });
        checkFilter(1, false, 0, true, 3.0 / 6.0 + 0.001, new double[] { 1, 1, 1, 1, 1, 1 });

        checkFilter(1, true, 2.01/17.0, true, 6.99/17.0,
                    new double[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, /**/ 0, 0, 1, 1, 1, 1, 0, 0 });
        checkFilter(1, true, 1.99/17.0, true, 6.99/17.0,
                    new double[] { 0, 0, 1, 1, 1, 1, 1, 0, 0, /**/ 0, 0, 1, 1, 1, 1, 1, 0 });
        checkFilter(1, true, 2.01/17.0, true, 7.01/17.0,
                    new double[] { 0, 0, 0, 1, 1, 1, 1, 1, 0, /**/ 0, 1, 1, 1, 1, 1, 0, 0 });
        checkFilter(1, true, 1.99/17.0, true, 7.01/17.0,
                    new double[] { 0, 0, 1, 1, 1, 1, 1, 1, 0, /**/ 0, 1, 1, 1, 1, 1, 1, 0 });
        checkFilter(1, true, 4.99/17.0, true, 5.01/17.0,
                    new double[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, /**/ 0, 0, 0, 1, 0, 0, 0, 0 });
        checkFilter(1, true, 5.01/17.0, true, 4.99/17.0,
                    new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, /**/ 0, 0, 0, 0, 0, 0, 0, 0 });

        checkFilter(1, false, 0, true, 4.99/16.0,
                    new double[] { 1, 1, 1, 1, 1, 0, 0, 0, /**/ 0, 0, 0, 0, 1, 1, 1, 1 });
        checkFilter(1, false, 0, true, 5.01/16.0,
                    new double[] { 1, 1, 1, 1, 1, 1, 0, 0, /**/ 0, 0, 0, 1, 1, 1, 1, 1 });
        checkFilter(2, false, 0, true, 4.99/16.0,
                    new double[] { 1, 1, 1, 0, 0, 0, 0, 0, /**/ 0, 0, 0, 0, 0, 0, 1, 1 });
        checkFilter(2, false, 0, true, 5.01/16.0,
                    new double[] { 1, 1, 1, 0, 0, 0, 0, 0, /**/ 0, 0, 0, 0, 0, 0, 1, 1 });
    }


    static private boolean isZero(Signal s) {
        final int N = s.getLength();

        // Only test the central portion
        for (int i = N/5; i < 4*N/5; i++) {
            if (s.getValue(i).getModulus() > 1) {
                return false;
            }
        }
        return true;
    }
    

    @Test
    @Grade(value = 1)
    public void testFilter() throws IOException {
        final EDFTimeSeries edf = new EDFTimeSeries(HttpToolbox.readResource("/test_generator_2.edf"));
        int channelIndex = edf.lookupChannelIndex("sine 8 Hz");
        Signal output = App.filter(edf, channelIndex, false, 0, false, 0);
        assertFalse(isZero(output));
        output = App.filter(edf, channelIndex, false, 0, true, 8.1);
        assertFalse(isZero(output));
        output = App.filter(edf, channelIndex, false, 0, true, 7.9);
        assertTrue(isZero(output));
        output = App.filter(edf, channelIndex, true, 7.9, false, 0);
        assertFalse(isZero(output));
        output = App.filter(edf, channelIndex, true, 8.1, false, 0);
        assertTrue(isZero(output));
    }
}
