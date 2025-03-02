import com.sun.net.httpserver.HttpExchange;

import be.uclouvain.ComplexNumber;
import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;
import be.uclouvain.Signal;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import static java.lang.Math.*;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF file to be used in the context of this project:
 * "test_generator_2.edf".
 **/
public class App {
    

    /**
     * This method must compute the Discrete Fourier Transform (DFT)
     * of one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be quadratic in the number of
     * elements in the input vector.
     *
     * @param The input signal in the time domain.
     * @return The output of the DFT, i.e. the Fourier spectrum.
     **/
    public static Signal computeDFT(Signal source) {

        int size = source.getLength();

        Signal fourier = new Signal(size);

        for (int k=0; k<size; k++) {

            ComplexNumber i = new ComplexNumber(0, 1);

            ComplexNumber somme = new ComplexNumber(0, 0);

            for (int n=0; n<size; n++) {

                double real = source.getValue(n).getReal() * exp((-2 * i.getReal() * PI * k * n ) / size);

                double imag = source.getValue(n).getImag() * exp((-2 * i.getImag() * PI * k * n ) / size);

                ComplexNumber current = new ComplexNumber(real, imag);

                somme = new ComplexNumber(somme.getReal()+current.getReal(), somme.getImag()+current.getImag());
            }

            fourier.setValue(k, somme);

        }


        return fourier;
    }
    

    /**
     * This method must compute the Fast Fourier Transform (FFT) of
     * one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be O(N * log(N)), where N is
     * the number of elements in the vector.
     *
     * N must be a power of 2. If it is not the case, an
     * "IllegalArgumentException" must be thrown.
     *
     * @param The input signal in the time domain.
     * @return The output of the FFT, i.e. the Fourier spectrum.
     **/
    public static Signal computeFFT(Signal source) {

        int size = source.getLength();

        if (size == 1) {
            Signal ret = new Signal(size);
            ret.setValue(0, source.getValue(0));
            return ret;
        }

        if (size % 2 != 0) {
            throw new IllegalArgumentException();
        }

        Signal even = new Signal(size/2);
        for (int k=0; k<size/2; k++) {
            even.setValue(k, source.getValue(2*k));
        }
        Signal evenFFT = computeFFT(even);

        Signal odd = even;
        for (int k=0; k<size/2; k++){
            odd.setValue(k, source.getValue(2*k+1));
        }
        Signal oddFFT = computeFFT(odd);

        Signal y = new Signal(size);
        for (int k=0; k<size/2; k++) {
            double kth = -2 * k * Math.PI / size;
            ComplexNumber wk = new ComplexNumber(Math.cos(kth), Math.sin(kth));

            ComplexNumber times = new ComplexNumber(wk.getReal() * oddFFT.getValue(k).getReal() - wk.getImag() * oddFFT.getValue(k).getImag(),
                    wk.getReal() * oddFFT.getValue(k).getImag() + wk.getImag() * oddFFT.getValue(k).getReal());

            y.setValue(k, new ComplexNumber(evenFFT.getValue(k).getReal() + times.getReal(),
                    evenFFT.getValue(k).getImag() + times.getImag()));

            y.setValue(k + size/2, new ComplexNumber(evenFFT.getValue(k).getReal() - times.getReal(),
                    evenFFT.getValue(k).getImag() - times.getImag()));
        }

        return y;
    }


    /**
     * This method must compute the power spectrum of one vector of
     * complex numbers. The normalization constant must be "T/N",
     * where "T" is the sampling period (in seconds), and "N" is the
     * number of elements in the vector. The output must be an array
     * of doubles containing the values of the power spectrum.
     **/
    public static double[] computePowerSpectrum(Signal fourier,
                                                double samplingFrequency) {

        int n = fourier.getLength();

        double[] powerSpectrum = new double[n];

        for (int k=0; k<n; k++) {

            powerSpectrum[k] = (samplingFrequency/n) * pow(abs(fourier.getValue(k).getReal()), 2);

        }

        return powerSpectrum;
    }


    /**
     * This method must implement a POST route in the REST API that
     * computes the power spectrum of one channel of the provided EDF
     * file. You must use the FFT (because DFT would take too much
     * time to run).
     *
     * If the length of the channel of interest is not a power of 2,
     * it must be padded with zeros. If there are "N" samples in the
     * channel, the function must only output the "N/2" first values
     * of the power spectrum (because the "N/2" next values are a
     * reversed copy of the first ones, by virtue of the properties of
     * the DFT of real-valued signals).
     *
     * Note that you don't have to implement the uploading of the EDF
     * file, neither the parsing of the channel index by yourself:
     * This is already implemented for you in the "AppLauncher.java"
     * file. Consequently, the "exchange" argument must only be used
     * to send data to the client of the REST API (in other words,
     * "exchange" must *not* be used as an input, but only as an
     * output).
     * 
     * The response must contain a JSON array that contains the power
     * spectrum of the channel of interest in the EDF file. More
     * precisely, each element of the JSON array must be a JSON
     * dictionary with two fields: "x" indicates the frequency
     * (expressed in Hertz), and "y" indicates the value of the power
     * spectrum at that frequency (after normalization by "T/N"). Pay
     * attention to the fact that "x" must take into account the
     * sampling frequency of the channel of interest.
     *
     * Sample command-line session using the "curl" tool to get the
     * power spectrum of the 6th channel in an EDF file (where
     * "test_generator_2.edf" corresponds to some EDF file in the
     * current directory):
     *
     *   $ curl http://localhost:8000/upload -F data=@test_generator_2.edf
     *   $ curl http://localhost:8000/compute-power-spectrum -d '{"channel":6}'
     *   [
     *     {
     *       "x": 0,
     *       "y": 0.12789769243681803
     *     },
     *     {
     *       "x": 0.00152587890625,
     *       "y": 0.002706872936643377
     *     },
     *     [...],
     *     {
     *       "x": 100,
     *       "y": 0
     *     }
     *   ]
     *
     **/
    public static void computePowerSpectrum(HttpExchange exchange,
                                            EDFTimeSeries timeSeries,
                                            int channelIndex) throws IOException {

        float samplingFrequency = timeSeries.getSamplingFrequency(channelIndex);

//        float numberOfSamples = timeSeries.getNumberOfSamples(channelIndex);

        JSONArray response = new JSONArray();

        Signal source = new Signal(timeSeries.getNumberOfSamples(channelIndex));
        System.out.println("samples: "+timeSeries.getNumberOfSamples(channelIndex));

        for (int i=0; i<source.getLength(); i++) {
            ComplexNumber current = new ComplexNumber(timeSeries.getChannel(channelIndex).getPhysicalValue(timeSeries.getDigitalValue(channelIndex, i)), 0);

//            System.out.println("i: "+i+" current: "+current.getReal());

            source.setValue(i, current);
        }

        Signal fourier = computeFFT(source);

        double[] powerSpectrum = computePowerSpectrum(fourier, samplingFrequency);

//        System.out.println("ici: "+powerSpectrum.length);

        for (int i=0; i<powerSpectrum.length; i++) {

            JSONObject dictionary = new JSONObject();

//            System.out.println("oui: "+samplingFrequency/powerSpectrum.length * i);

            dictionary.put("x", samplingFrequency/powerSpectrum.length * i);

            dictionary.put("y", powerSpectrum[i]);

            response.put(dictionary);

        }

        HttpToolbox.sendResponse(exchange, response);
    }
}
