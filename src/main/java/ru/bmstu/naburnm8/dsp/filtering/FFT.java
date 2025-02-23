package ru.bmstu.naburnm8.dsp.filtering;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

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
            abs[i] = 20*Math.log10(fftResults[i].abs()) / 7;
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

    public static double[] convolve(double[] input, double[] kernel) {
        int inputLength = input.length;
        int kernelLength = kernel.length;
        int outputLength = inputLength + kernelLength - 1;
        double[] paddedInput = new double[nextPowerOf2(outputLength)];
        double[] paddedKernel = new double[nextPowerOf2(outputLength)];
        System.arraycopy(input, 0, paddedInput, 0, inputLength);
        System.arraycopy(kernel, 0, paddedKernel, 0, kernelLength);

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] inputFFT = fft.transform(paddedInput, TransformType.FORWARD);
        Complex[] kernelFFT = fft.transform(paddedKernel, TransformType.FORWARD);
        Complex[] outputFFT = new Complex[nextPowerOf2(outputLength)];
        for (int i = 0; i < nextPowerOf2(outputLength); i++) {
            outputFFT[i] = inputFFT[i].multiply(kernelFFT[i]);
        }

        // Perform inverse FFT to get the convolved signal
        Complex[] outputComplex = fft.transform(outputFFT, TransformType.INVERSE);

        double[] output = new double[outputLength];
        for (int i = 0; i < outputLength; i++) {
            output[i] = outputComplex[i].getReal();
        }

        return output;
    }
}
