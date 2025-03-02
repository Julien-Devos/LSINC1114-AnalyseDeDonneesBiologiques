import be.uclouvain.ComplexNumber;
import be.uclouvain.EDFTimeSeries;
import be.uclouvain.Signal;

import java.io.IOException;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF files to be used in the context of this project:
 * "test_generator_2.edf" and "slow_drifts.edf".
 **/
public class App {


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
     * This is the same function as in homework "04-ComputeFourier".
     *
     * @param source The input signal in the time domain.
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
//        System.out.println("even size: "+even.getLength());
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
     * This method must compute the Inverse Fast Fourier Transform of
     * one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be O(N * log(N)), where N is
     * the number of elements in the vector.
     *
     * N must be a power of 2. If it is not the case, an
     * "IllegalArgumentException" must be thrown.
     *
     * @param source The input Fourier spectrum.
     * @return The output of the Inverse FFT, i.e. the signal in the time domain.
     **/
    public static Signal computeInverseFFT(Signal source) {

        int size = source.getLength();

        if (size % 2 != 0 && size != 1) {
            throw new IllegalArgumentException();
        }

        Signal y = new Signal(size);

        for (int i=0; i<size; i++) {
            y.setValue(i, new ComplexNumber(source.getValue(i).getReal(), - (source.getValue(i).getImag()) ));
        }

        y = computeFFT(y);

        for (int i=0; i<size; i++) {
            y.setValue(i, new ComplexNumber(y.getValue(i).getReal() / size, y.getValue(i).getImag() / size));
        }

        for (int i=0; i<size; i++) {
            y.setValue(i, new ComplexNumber(y.getValue(i).getReal(), - (y.getValue(i).getImag()) ));
        }

        return y;
    }


    /**
     * Returns the frequency that is associated with item of index "k"
     * (i.e. "g[k]") in a discretized filter applicable to a signal of
     * length "N" whose sampling frequency is "Fs". Check out the last
     * slide of Session 5.
     *
     * @param k The index of interest.
     * @param N The length of the signal.
     * @param Fs The sampling frequency of the signal (i.e. "1/T").
     * @return The frequency.
     **/
    public static double getFrequency(int k,
                                      int N,
                                      double Fs) {

        if (k == N || k < 0) {
            throw new IllegalArgumentException();
        }

        if (k <= N/2) {
            return k * Fs / N;
        } else {
            return (k-N) * Fs/N;
        }

    }


    /**
     * Creates discretized low-pass, high-pass or pass-band filter
     * applicable to a signal of length "N" by sampling an ideal
     * filter.
     *
     * If neither "hasHighpass", nor "hasLowpass" is true, the filter
     * must have a gain of 100% everywhere.
     *
     * If both "hasHighpass" and "hasLowpass" are true, the filter
     * must have a gain of 100% inside the range
     * "[highpassCutoff,lowpassCutoff]", and 0% outside of this range.
     * If "highpassCutoff > lowpassCutoff", the gain is 0% everywhere.
     *
     * If "hasHighpass" (resp. "hasLowpass") is the only Boolean to be
     * "true", the function must create a high-pass (resp. low-pass)
     * filter whose cutoff frequency is "highpassCutoff"
     * (resp. "lowpassCutoff").
     *
     * To implement this function, make sure to use the function
     * "getFrequency()" above.
     *
     * @param N The length of the signal.
     * @param Fs The sampling frequency of the signal (i.e. "1/T").
     * @param hasHighpass Whether a high-pass filter is to be generated.
     * @param highpassCutoff Cutoff frequency for high-pass filtering.
     * Only makes sense if "hasHighpass" is "true".
     * @param hasLowpass Whether a low-pass filter is to be generated.
     * @param lowpassCutoff Cutoff frequency for low-pass filtering.
     * Only makes sense if "hasLowpass" is "true".
     * @return The filter in the frequency domain.
     **/
    public static Signal createFilter(int N,
                                      double Fs,
                                      boolean hasHighpass,
                                      double highpassCutoff,
                                      boolean hasLowpass,
                                      double lowpassCutoff) {

        Signal filter = new Signal(N);

        if (! hasHighpass && ! hasLowpass) {

            for (int i=0; i<N; i++) {
                filter.setValue(i, new ComplexNumber(1));
            }

            return filter;
        }


        Signal frequency = new Signal(N);
        for (int k=0; k<N; k++) {
            frequency.setValue(k, new ComplexNumber(Math.abs(getFrequency(k, N, Fs))));
        }

        if (hasHighpass && hasLowpass) {

            if (highpassCutoff > lowpassCutoff) {

                for (int i=0; i<N; i++) {

                    filter.setValue(i, new ComplexNumber(0));

                }

            } else {

                for (int i=0; i<N; i++) {

                    if (frequency.getValue(i).getReal() < highpassCutoff || frequency.getValue(i).getReal() > lowpassCutoff) {
                        filter.setValue(i, new ComplexNumber(0));
                    } else {
                        filter.setValue(i, new ComplexNumber(1));
                    }

                }

            }

        } else if (hasHighpass) {

            for (int i=0; i<N; i++) {

                if (frequency.getValue(i).getReal() <= highpassCutoff) {
                    filter.setValue(i, new ComplexNumber(0));
                } else {
                    filter.setValue(i, new ComplexNumber(1));
                }

            }

        } else {

            for (int i=0; i<N; i++) {

                if (frequency.getValue(i).getReal() >= lowpassCutoff) {
                    filter.setValue(i, new ComplexNumber(0));
                } else {
                    filter.setValue(i, new ComplexNumber(1));
                }

            }

        }

        return filter;
    }


    /**
     * Apply an ideal low-pass, high-pass or pass-band filter to one
     * channel in some EEG time series. The conventions for specifying
     * the filter using the arguments of the function are the same as
     * in function "createFilter()".
     *
     * The input signal must be padded with zeros until its length
     * corresponds to a power of 2. The output signal must be cropped
     * to the original length of the signal (i.e. the filtered items
     * corresponding to the padded zeros must be removed).
     *
     * @param timeSeries The EEG data.
     * @param channelIndex The index of the channel of interest.
     * @param hasHighpass Whether a high-pass filter is to be used.
     * @param highpassCutoff Cutoff frequency for high-pass filtering.
     * Only makes sense if "hasHighpass" is "true".
     * @param hasLowpass Whether a low-pass filter is to be used.
     * @param lowpassCutoff Cutoff frequency for low-pass filtering.
     * Only makes sense if "hasLowpass" is "true".
     * @return The filtered EEG channel.
     * @see #createFilter(int, double, boolean, double, boolean, double)
     **/
    public static Signal filter(EDFTimeSeries timeSeries,
                                int channelIndex,
                                boolean hasHighpass,
                                double highpassCutoff,
                                boolean hasLowpass,
                                double lowpassCutoff) throws IOException {

        EDFTimeSeries.Channel chan = timeSeries.getChannel(channelIndex);

        int numberOfSamples = timeSeries.getNumberOfSamples(channelIndex);

        int newSize = numberOfSamples;

        if ( ! (numberOfSamples > 0 && (numberOfSamples & (numberOfSamples - 1)) == 0) ) {

            newSize = 1;

            while (newSize <= numberOfSamples) {
                newSize *= 2;
            }

        }

        Signal source = new Signal(newSize);
        for (int i=0; i<numberOfSamples; i++) {
            source.setValue(i, chan.getPhysicalValue(timeSeries.getDigitalValue(channelIndex, i)));
        }

        if ( newSize != numberOfSamples ) {

            for (int i=numberOfSamples; i<newSize; i++) {
                source.setValue(i, 0);
            }

        }


        Signal fourier = computeFFT(source);


        Signal filter = createFilter(newSize, timeSeries.getSamplingFrequency(channelIndex), hasHighpass, highpassCutoff, hasLowpass, lowpassCutoff);

        for (int i=0; i<fourier.getLength(); i++) {

            fourier.setValue(i, new ComplexNumber(fourier.getValue(i).getReal() * filter.getValue(i).getReal(), fourier.getValue(i).getImag() * filter.getValue(i).getReal()));

        }

        fourier = computeInverseFFT(fourier);

        Signal outputSignal = new Signal(numberOfSamples);
        for (int i=0; i<numberOfSamples; i++) {

            outputSignal.setValue(i, new ComplexNumber(fourier.getValue(i).getReal()));

        }

        return outputSignal;
    }
}
