package ru.bmstu.naburnm8.dsp.filtering;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.ceil;


public class FFT {
    public static double[] toFrequencySpace(short[] samples){
        short[] samplesOut = samples;
        if (!isPowerOf2(samples.length)){
            int nextPowerOf2 = nextPowerOf2(samples.length);
            samplesOut = padWithZeros(samples, nextPowerOf2);
        }
        return getAbs(applyFFTLIB(samplesOut));
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
        double[] abs = new double[fftResults.length / 4];
        for (int i = 0; i < abs.length; i++) {
            abs[i] = 20*Math.log10(fftResults[i].abs());
        }
        return abs;
    }

    public static boolean isPowerOf2(int n){
        if (n == 0) {
            return false;
        }
        double v = Math.log(n) / Math.log(2);
        return (int)(Math.ceil(v)) == (int)(Math.floor(v));
    }
    private static int nextPowerOf2(int n) {
        int power = 1;
        while (power < n) {
            power <<= 1;
        }
        return power;
    }
    private static short[] padWithZeros(short[] samples, int targetLength) {
        short[] paddedSamples = new short[targetLength];
        System.arraycopy(samples, 0, paddedSamples, 0, samples.length);
        return paddedSamples;
    }
}
