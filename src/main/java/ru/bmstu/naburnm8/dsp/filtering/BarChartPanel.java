package ru.bmstu.naburnm8.dsp.filtering;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class BarChartPanel extends JPanel {
    private double[] data;
    private Color barColor;
    private String title;

    public BarChartPanel(double[] data, Color barColor, String title) {
        this.data = data;
        this.barColor = barColor;
        this.title = title;
    }

    public void setData(double[] newData) {
        this.data = newData;
        repaint(); // Trigger a repaint to update the chart
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.length == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int barWidth = width / data.length;
        int maxBarHeight = height - 50; // Leave space for title and labels

        // Find the maximum value in the dataset to scale the bars
        double maxValue = Arrays.stream(data).max().orElse(1);

        // Draw the title
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, 20);

        // Draw the bars
        g.setColor(barColor);
        for (int i = 0; i < data.length; i++) {
            int barHeight = (int) ((data[i] / maxValue) * maxBarHeight);
            int x = i * barWidth;
            int y = height - barHeight - 30; // Leave space for labels
            g.fillRect(x, y, barWidth - 2, barHeight); // Subtract 2 for spacing between bars

            // Draw the value label
            g.setColor(Color.BLACK);
            String valueLabel = String.format("%.2f", data[i]);
            int labelWidth = g.getFontMetrics().stringWidth(valueLabel);
            g.drawString(valueLabel, x + (barWidth - labelWidth) / 2, y - 5);
            g.setColor(barColor);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bar Chart Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        double[] initialData = {10, 20, 30, 40, 50};
        BarChartPanel barChartPanel = new BarChartPanel(initialData, Color.BLUE, "Sample Bar Chart");
        frame.add(barChartPanel);

        frame.setVisible(true);

        // Example of updating the dataset after a delay
        new Timer(3000, e -> {
            double[] newData = {15, 25, 35, 45, 55};
            barChartPanel.setData(newData);
        }).start();
    }
}