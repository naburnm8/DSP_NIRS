package ru.bmstu.naburnm8.dsp.filtering;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class Filter implements Callable<short[]> {
    public static double db2Linear(int db){
        return Math.pow(10, ((double) db/20));
    }
    public static double[] computeImpulseResponse(double[] numerator, double[] denominator, int length){
        double[] impulseResponse = new double[length];
        double[] input = new double[length];
        input[0] = 1.0;

        for (int n = 0; n < length; n++) {
            double output = 0.0;

            for (int k = 0; k < numerator.length; k++) {
                if (n - k >= 0) {
                    output += numerator[k] * input[n - k];
                }
            }

            for (int k = 1; k < denominator.length; k++) {
                if (n - k >= 0) {
                    output -= denominator[k] * impulseResponse[n - k];
                }
            }

            impulseResponse[n] = output / denominator[0];
        }

        return impulseResponse;
    }

    private double level;
    private final double[] impulseResponse;
    private final String type;
    private short[] inputData;

    public Filter(double[] numerator, double[] denominator, int impulseResponseLength){
        this.level = 1;
        this.impulseResponse = computeImpulseResponse(numerator, denominator, impulseResponseLength);
        this.type = "IIR";
    }

    public Filter(double[] impulseResponse){
        this.level = 1;
        this.impulseResponse = impulseResponse;
        this.type = "FIR";
    }

    public String getType(){
        return type;
    }


    public double getLevel() {
        return level;
    }

    public void setLevelInDB(int dBLevel) {
        this.level = db2Linear(dBLevel);
    }

    public double[] getImpulseResponse() {
        return impulseResponse;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "level=" + level + "}";

    }

    public short[] convolve(short[] input){
        short[] output = new short[input.length];
        for (int i = 0; i < input.length - this.impulseResponse.length; i++) {
            for (int j = 0; j < this.impulseResponse.length; j++) {
                int multi = (int) (input[i] * this.impulseResponse[j]);
                output[i+j] += (short) (multi * level);
            }
        }
        return output;
    }

    public short[] convolveByFFT(short[] input){
        short[] output = new short[input.length];
        int irLength = this.impulseResponse.length;

        // Use FFT-based convolution for efficiency
        double[] inputDouble = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            inputDouble[i] = input[i];
        }

        double[] outputDouble = FFT.convolve(inputDouble, this.impulseResponse);

        for (int i = 0; i < output.length; i++) {
            output[i] = (short) (outputDouble[i] * level);
        }

        return output;
    }

    @Override
    public short[] call() throws Exception {
       return this.convolveByFFT(inputData);
    }

    public short[] getInputData() {
        return inputData;
    }

    public void setInputData(short[] inputData) {
        this.inputData = inputData;
    }
}
