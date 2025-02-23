package ru.bmstu.naburnm8.dsp.files;

import ru.bmstu.naburnm8.dsp.filtering.Filter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterParser {

    public static List<Filter> parseFilters(boolean parseIIR){
        ArrayList<Filter> filters = new ArrayList<>();
        if (parseIIR) {
            String path = "src/main/resources/IIR/";
            for (int i = 0; i < 10; i++){
                String currentPath = path + "band" + i + ".fcf";
                try {
                    Filter filter = parseIIRFilter(currentPath, 4000);
                    filters.add(filter);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                    return new ArrayList<>();
                }
            }
        }
        else {
            String path = "src/main/resources/FIR/";
            for (int i = 0; i < 10; i++){
                String currentPath = path + "band" + i + ".fcf";
                try {
                    Filter filter = parseFIRFilter(currentPath);
                    filters.add(filter);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                    return new ArrayList<>();
                }
            }
        }
        return filters;
    }
    public static Filter parseFIRFilter(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        ArrayList<Double> numerator = new ArrayList<>();
        boolean isNumerator = false;

        String line;
        while((line = reader.readLine()) != null){
            line = line.trim();

            if (line.startsWith("Numerator:")) {
                isNumerator = true;
                continue;
            }

            if (line.isEmpty() || line.startsWith("%")) {
                continue;
            }

            if (isNumerator) {
                numerator.add(Double.parseDouble(line));
            }
        }
        reader.close();

        double[] numeratorArray = new double[numerator.size()];
        for (int i = 0; i < numeratorArray.length; i++){
            numeratorArray[i] = numerator.get(i);
        }
        return new Filter(numeratorArray);
    }

    public static Filter parseIIRFilter(String path, int impulseResponseLength) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        ArrayList<Double> numerator = new ArrayList<>();
        ArrayList<Double> denominator = new ArrayList<>();
        boolean isNumerator = false;
        boolean isDenominator = false;

        String line;
        while((line = reader.readLine()) != null){
            line = line.trim();

            if (line.startsWith("Numerator:")) {
                isNumerator = true;
                isDenominator = false;
                continue;
            } else if (line.startsWith("Denominator:")) {
                isNumerator = false;
                isDenominator = true;
                continue;
            }

            if (line.isEmpty() || line.startsWith("%")) {
                continue;
            }

            if (isNumerator) {
                numerator.add(Double.parseDouble(line));
            } else if (isDenominator) {
                denominator.add(Double.parseDouble(line));
            }
        }
        reader.close();

        double[] numeratorArray = new double[numerator.size()];
        double[] denominatorArray = new double[denominator.size()];
        for (int i = 0; i < numeratorArray.length; i++){
            numeratorArray[i] = numerator.get(i);
        }
        for (int i = 0; i < denominatorArray.length; i++){
            denominatorArray[i] = denominator.get(i);
        }
        return new Filter(numeratorArray, denominatorArray, impulseResponseLength);
    }
    public static void main(String[] args) {
        String path = "src/main/resources/FIR/band0.fcf";
        try {
            //Filter parsed = parseIIRFilter(path, 90);
            Filter parsed = parseFIRFilter(path);
            System.out.println(parsed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
