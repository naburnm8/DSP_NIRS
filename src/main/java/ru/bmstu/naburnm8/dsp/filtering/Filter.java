package ru.bmstu.naburnm8.dsp.filtering;

import java.util.Arrays;

public class Filter {
    public static double db2Linear(int db){
        return Math.pow(10, ((double) db/20));
    }
    public static double[] computeImpulseResponse(double[] numerator, double[] denominator, int length){
        double[] impulseResponse = new double[length];
        double[] input = new double[length];
        input[0] = 1.0; // Impulse input

        // Apply the filter to the impulse input
        for (int n = 0; n < length; n++) {
            double output = 0.0;

            // Compute the output using the difference equation
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

    public Filter(double[] numerator, double[] denominator, int RING_BUF_LENGTH){
        this.level = 1;
        this.impulseResponse = computeImpulseResponse(numerator, denominator, RING_BUF_LENGTH / 2);
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
}
