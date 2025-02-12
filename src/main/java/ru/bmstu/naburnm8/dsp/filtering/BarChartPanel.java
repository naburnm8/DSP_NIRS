package ru.bmstu.naburnm8.dsp.filtering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

public class BarChartPanel extends JPanel implements ComponentListener{
    private double[] data;
    private Color barColor;
    private final String title;
    private int maxBarsToDisplay;

    public BarChartPanel(double[] data, Color barColor, String title, int maxBarsToDisplay) {
        this.data = data;
        this.barColor = barColor;
        this.title = title;
        this.maxBarsToDisplay = maxBarsToDisplay;
        this.addComponentListener(this);
    }

    public void setData(double[] newData) {
        this.data = newData;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.length == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int maxBarHeight = height - 50;

        //System.out.println(width);

        double[] aggregatedData = aggregateData(data, maxBarsToDisplay);

        int barWidth = width / aggregatedData.length;
        int totalBarsWidth = aggregatedData.length * barWidth;
        int startX = (width - totalBarsWidth) / 2;

        double maxValue = Arrays.stream(aggregatedData).max().orElse(1);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, 20);

        g.setColor(barColor);
        for (int i = 0; i < aggregatedData.length; i++) {
            int barHeight = (int) ((aggregatedData[i] / maxValue) * maxBarHeight);
            int x = startX + i * barWidth;
            int y = height - barHeight - 30;
            g.fillRect(x, y, barWidth , barHeight);

            if (aggregatedData.length <= 50) {
                g.setColor(Color.BLACK);
                String valueLabel = String.format("%.2f", aggregatedData[i]);
                int labelWidth = g.getFontMetrics().stringWidth(valueLabel);
                g.drawString(valueLabel, x + (barWidth - labelWidth) / 2, y - 5);
                g.setColor(barColor);
            }
        }
    }

    private double[] aggregateData(double[] data, int maxBarsToDisplay) {
        if (data.length <= maxBarsToDisplay) {
            return data;
        }

        double[] aggregatedData = new double[maxBarsToDisplay];
        int binSize = data.length / maxBarsToDisplay;

        for (int i = 0; i < maxBarsToDisplay; i++) {
            double sum = 0;
            int start = i * binSize;
            int end = (i == maxBarsToDisplay - 1) ? data.length : start + binSize;

            for (int j = start; j < end; j++) {
                sum += data[j];
            }
            aggregatedData[i] = sum / (end - start);
            // using max values
            // aggregatedData[i] = Arrays.stream(data, start, end).max().orElse(0);
        }

        return aggregatedData;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bar Chart Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);


        double[] initialData = new double[32768];
        for (int i = 0; i < initialData.length; i++) {
            initialData[i] = Math.sin(i * 0.01) * 100 + 100; // Example data
        }

        BarChartPanel barChartPanel = new BarChartPanel(initialData, Color.BLUE, "Large Dataset Bar Chart", 100);
        frame.add(barChartPanel);

        frame.setVisible(true);


        new Timer(3000, e -> {
            double[] newData = new double[32768];
            for (int i = 0; i < newData.length; i++) {
                newData[i] = Math.cos(i * 0.01) * 100 + 100; // New example data
            }
            barChartPanel.setData(newData);
        }).start();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (maxBarsToDisplay > this.getWidth() || this.getWidth() / maxBarsToDisplay > 1) {
            maxBarsToDisplay = this.getWidth();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}