package ru.bmstu.naburnm8.dsp.filtering;

public class FilterOptimum extends Filter {

    public FilterOptimum(double[] numerator, double[] denominator, int impulseResponseLength) {
        super(numerator, denominator, impulseResponseLength);
    }

    public FilterOptimum(double[] impulseResponse) {
        super(impulseResponse);
    }

    @Override
    public short[] call() throws Exception {
        return this.convolveByFFT(inputData);
    }
}
