package ru.bmstu.naburnm8.dsp.filtering;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;

public class FFT {
    public static double[] toFrequencySpace(short[] samples){
        return getAbs(applyFFTLIB(samples));
    }
    private static Complex[] applyFFTLIB(short[] samples){
        int n = samples.length;
        double[] doubleSamples = new double[n];
        for (int i = 0; i < n; i++) {
            doubleSamples[i] = samples[i];
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        return fft.transform(doubleSamples, null);
    }
    private static double[] getAbs(Complex[] fftResults){
        double[] abs = new double[fftResults.length / 2];
        for (int i = 0; i < abs.length; i++) {
            abs[i] = Math.log10(fftResults[i].abs());
        }
        return abs;
    }
}
