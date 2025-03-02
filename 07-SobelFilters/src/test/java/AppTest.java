import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import be.uclouvain.DicomImage;
import be.uclouvain.HttpToolbox;
import be.uclouvain.MockMatrixTools;

import java.io.IOException;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    @Test
    @Grade(value = 1)
    public void testRangeNormalization() {
        RealMatrix m = MatrixUtils.createRealMatrix(1, 3);
        m.setEntry(0, 0, -1000);
        m.setEntry(0, 1, 0);
        m.setEntry(0, 2, 600);
        
        RealMatrix c = App.rangeNormalization(m);
        assertEquals(3, c.getColumnDimension());
        assertEquals(1, c.getRowDimension());
        assertEquals(0.0, c.getEntry(0, 0), 0.0001);
        assertEquals(159.375, c.getEntry(0, 1), 0.0001);
        assertEquals(255.0, c.getEntry(0, 2), 0.0001);
    }

    @Test
    @Grade(value = 1)
    public void testKernel1x1() {
        RealMatrix m = MatrixUtils.createRealMatrix(2, 3);
        m.setEntry(0, 0, 42);
        m.setEntry(0, 1, 43);
        m.setEntry(0, 2, 44);
        m.setEntry(1, 0, 45);
        m.setEntry(1, 1, 46);
        m.setEntry(1, 2, 47);
        
        RealMatrix k = MatrixUtils.createRealMatrix(1, 1);
        k.setEntry(0, 0, 1);

        RealMatrix c = App.convolve(m, k);
        assertEquals(3, c.getColumnDimension());
        assertEquals(2, c.getRowDimension());
        assertEquals(42.0, c.getEntry(0, 0), 0.0001);
        assertEquals(43.0, c.getEntry(0, 1), 0.0001);
        assertEquals(44.0, c.getEntry(0, 2), 0.0001);
        assertEquals(45.0, c.getEntry(1, 0), 0.0001);
        assertEquals(46.0, c.getEntry(1, 1), 0.0001);
        assertEquals(47.0, c.getEntry(1, 2), 0.0001);

        m = MatrixUtils.createRealMatrix(1, 1);
        m.setEntry(0, 0, 42);
        c = App.convolve(m, k);
        assertEquals(1, c.getColumnDimension());
        assertEquals(1, c.getRowDimension());
        assertEquals(42.0, c.getEntry(0, 0), 0.0001);
    }

    @Test
    @Grade(value = 1)
    public void testKernel1x3() {
        /**
           Under Octave:

           a=[42 43 44 ; 15 16 27 ; 28 29 20]; 
           k=[-1 0 1]
           dx=conv2(a,flip(flip(k,1),2),'valid')

         **/

        RealMatrix m = MatrixUtils.createRealMatrix(3, 3);
        m.setEntry(0, 0, 42);
        m.setEntry(0, 1, 43);
        m.setEntry(0, 2, 44);
        m.setEntry(1, 0, 15);
        m.setEntry(1, 1, 16);
        m.setEntry(1, 2, 27);
        m.setEntry(2, 0, 28);
        m.setEntry(2, 1, 29);
        m.setEntry(2, 2, 20);
        
        RealMatrix k = MatrixUtils.createRealMatrix(1, 3);
        k.setEntry(0, 0, -1);
        k.setEntry(0, 1, 0);
        k.setEntry(0, 2, 1);

        RealMatrix c = App.convolve(m, k);
        assertEquals(1, c.getColumnDimension());
        assertEquals(3, c.getRowDimension());
        assertEquals(2.0, c.getEntry(0, 0), 0.0001);
        assertEquals(12.0, c.getEntry(1, 0), 0.0001);
        assertEquals(-8.0, c.getEntry(2, 0), 0.0001);
    }

    @Test
    @Grade(value = 1)
    public void testKernel3x1() {
        /**
           Under Octave:

           a=[42 43 44 ; 15 16 27 ; 28 29 20]; 
           k=[-1 ; 0 ; 1]
           dx=conv2(a,flip(flip(k,1),2),'valid')

         **/

        RealMatrix m = MatrixUtils.createRealMatrix(3, 3);
        m.setEntry(0, 0, 42);
        m.setEntry(0, 1, 43);
        m.setEntry(0, 2, 44);
        m.setEntry(1, 0, 15);
        m.setEntry(1, 1, 16);
        m.setEntry(1, 2, 27);
        m.setEntry(2, 0, 28);
        m.setEntry(2, 1, 29);
        m.setEntry(2, 2, 20);
        
        RealMatrix k = MatrixUtils.createRealMatrix(3, 1);
        k.setEntry(0, 0, -1);
        k.setEntry(1, 0, 0);
        k.setEntry(2, 0, 1);

        RealMatrix c = App.convolve(m, k);
        assertEquals(3, c.getColumnDimension());
        assertEquals(1, c.getRowDimension());
        assertEquals(-14.0, c.getEntry(0, 0), 0.0001);
        assertEquals(-14.0, c.getEntry(0, 1), 0.0001);
        assertEquals(-24.0, c.getEntry(0, 2), 0.0001);
    }

    @Test
    @Grade(value = 1)
    public void testSobelBasics() {
        /**
           Under Octave:

           a=[42 43 44 ; 15 16 27 ; 28 29 20]; 
           kx=[-1 0 1; -2 0 2; -1 0 1]; 
           ky=[-1 -2 -1; 0 0 0; 1 2 1]; 
           dx=conv2(a,flip(flip(kx,1),2),'valid')
           dy=conv2(a,flip(flip(ky,1),2),'valid')
           magnitude=abs(dx)+abs(dy)

         **/
        
        RealMatrix m = MatrixUtils.createRealMatrix(3, 3);
        m.setEntry(0, 0, 42);
        m.setEntry(0, 1, 43);
        m.setEntry(0, 2, 44);
        m.setEntry(1, 0, 15);
        m.setEntry(1, 1, 16);
        m.setEntry(1, 2, 27);
        m.setEntry(2, 0, 28);
        m.setEntry(2, 1, 29);
        m.setEntry(2, 2, 20);

        RealMatrix c = App.sobelX(m);
        assertEquals(1, c.getColumnDimension());
        assertEquals(1, c.getRowDimension());
        assertEquals(18.0, c.getEntry(0, 0), 0.0001);

        c = App.sobelY(m);
        assertEquals(1, c.getColumnDimension());
        assertEquals(1, c.getRowDimension());
        assertEquals(-66.0, c.getEntry(0, 0), 0.0001);

        c = App.sobelMagnitude(m);
        assertEquals(1, c.getColumnDimension());
        assertEquals(1, c.getRowDimension());
        assertEquals(84.0, c.getEntry(0, 0), 0.0001);
    }

    @Test
    @Grade(value = 1)
    public void testEdgeCases() {
        try {
            App.convolve(MatrixUtils.createRealMatrix(1, 1), MatrixUtils.createRealMatrix(1, 3));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            App.convolve(MatrixUtils.createRealMatrix(1, 2), MatrixUtils.createRealMatrix(1, 3));
            fail();
        } catch (IllegalArgumentException e) {
        }

        App.convolve(MatrixUtils.createRealMatrix(1, 3), MatrixUtils.createRealMatrix(1, 3));

        try {
            App.convolve(MatrixUtils.createRealMatrix(1, 1), MatrixUtils.createRealMatrix(3, 1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            App.convolve(MatrixUtils.createRealMatrix(2, 1), MatrixUtils.createRealMatrix(3, 1));
            fail();
        } catch (IllegalArgumentException e) {
        }
        
        App.convolve(MatrixUtils.createRealMatrix(3, 1), MatrixUtils.createRealMatrix(3, 1));

        for (int height = 0; height < 4; height++) {
            for (int width = 0; width < 4; width++) {
                if (width != 3 || height != 3) {
                    try {
                        App.convolve(MatrixUtils.createRealMatrix(height, width), MatrixUtils.createRealMatrix(3, 3));
                        fail();
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }

        App.convolve(MatrixUtils.createRealMatrix(3, 3), MatrixUtils.createRealMatrix(3, 3));
    }

    @Test
    @Grade(value = 1)
    public void testHand() throws IOException {
        RealMatrix dicom = DicomImage.createFromResource("/hand.dcm").getFloatPixelData();

        RealMatrix a = MockMatrixTools.readMatrixFromPython(HttpToolbox.getResourceStream("/hand.matrix"));
        assertTrue(MockMatrixTools.compareMatrices(dicom, a));

        a = MockMatrixTools.readMatrixFromPython(HttpToolbox.getResourceStream("/hand-sobel-x.matrix"));
        assertTrue(MockMatrixTools.compareMatrices(App.sobelX(dicom), a));

        a = MockMatrixTools.readMatrixFromPython(HttpToolbox.getResourceStream("/hand-sobel-y.matrix"));
        assertTrue(MockMatrixTools.compareMatrices(App.sobelY(dicom), a));

        a = MockMatrixTools.readMatrixFromPython(HttpToolbox.getResourceStream("/hand-sobel-magnitude.matrix"));
        assertTrue(MockMatrixTools.compareMatrices(App.sobelMagnitude(dicom), a));

        a = MockMatrixTools.readMatrixFromPython(HttpToolbox.getResourceStream("/hand-sobel-x-normalized.matrix"));
        assertTrue(MockMatrixTools.compareMatrices(App.rangeNormalization(App.sobelX(dicom)), a));
    }
}
