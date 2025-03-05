package ru.bmstu.naburnm8.dsp.experiments;

import ru.bmstu.naburnm8.dsp.files.AudioLoader;
import ru.bmstu.naburnm8.dsp.files.FilterParser;
import ru.bmstu.naburnm8.dsp.filtering.BarChartPanel;
import ru.bmstu.naburnm8.dsp.filtering.DataConverter;
import ru.bmstu.naburnm8.dsp.filtering.FFT;
import ru.bmstu.naburnm8.dsp.filtering.Filter;
import ru.bmstu.naburnm8.dsp.playback.Player;
import ru.bmstu.naburnm8.dsp.playback.RingBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class ExperimentCase {
    private String name;
    private int id;

    public ExperimentCase(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }
}

public class FilterOrderExperiment extends Component {
    private static final int RING_BUF_SIZE = 131072;
    private boolean playing = false;
    private String selectedFilePath;
    private Player player;
    private RingBuffer ringBuffer;
    private AudioLoader loader;
    private int lastLoaderBytes;
    private ArrayList<Filter> lPassIIR;
    private ArrayList<Filter> bPassIIR;
    private ArrayList<Filter> hPassIIR;
    private ArrayList<Filter> lPassFIR;
    private ArrayList<Filter> bPassFIR;
    private ArrayList<Filter> hPassFIR;

    private int filterMode = 0;
    private int filterQuality = 0;

    private final BarChartPanel barChartPanel;


    FilterOrderExperiment() {
        lPassIIR = new ArrayList<>(FilterParser.parseFiltersArgs(true, 1000, 3, "EXP_lpass", true));
        bPassIIR = new ArrayList<>(FilterParser.parseFiltersArgs(true, 1000, 3, "EXP_bpass", true));
        hPassIIR = new ArrayList<>(FilterParser.parseFiltersArgs(true, 1000, 3, "EXP_hpass", true));

        lPassFIR = new ArrayList<>(FilterParser.parseFiltersArgs(false, 1000, 3, "EXP_lpass", true));
        bPassFIR = new ArrayList<>(FilterParser.parseFiltersArgs(false, 1000, 3, "EXP_bpass", true));
        hPassFIR = new ArrayList<>(FilterParser.parseFiltersArgs(false, 1000, 3, "EXP_hpass", true));

        int frameWidth = (int)(0.6*800);
        double[] initialData = {10, 20, 30, 40, 50};
        int maxBarsToDisplay = (int)(frameWidth * 0.507);
        barChartPanel = new BarChartPanel(initialData, Color.BLUE, "Spectrum", maxBarsToDisplay);

        JFrame frame = new JFrame("Buffer Size Experiment");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,400);
        JButton playButton = new JButton("Play");
        JButton stopButton = new JButton("Stop");
        JButton pickButton = new JButton("Pick Wav");
        JList<Integer> qualityChooser = new JList<>(new Integer[]{0, 1, 2});
        JList<ExperimentCase> experimentChooser = new JList<>(new ExperimentCase[]{
                new ExperimentCase("Low pass IIR", 0),
                new ExperimentCase("Band pass IIR", 1),
                new ExperimentCase("High pass IIR", 2),
                new ExperimentCase("Low pass FIR", 3),
                new ExperimentCase("Band pass FIR", 4),
                new ExperimentCase("High pass FIR", 5),
        });

        qualityChooser.addListSelectionListener(l -> {
            filterQuality = qualityChooser.getSelectedIndex();
        });

        experimentChooser.addListSelectionListener(l -> {
            filterMode = experimentChooser.getSelectedIndex();
        });

        stopButton.addActionListener(e -> {
            playing = false;
        });

        pickButton.addActionListener(e -> {
            openFilePicker();
        });

        playButton.addActionListener(e -> {
            if (!playing) {
                if (selectedFilePath == null) {
                    return;
                }
                try{
                    initMusicPlayer();
                    if (loader.getAudioFormat().getSampleSizeInBits() != 16){
                        System.out.println("Audio Format Error");
                        return;
                    }
                    playing = true;
                    play();
                } catch (Exception e1) {
                    System.out.println(e1.getMessage());
                }
            }
        });
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        panel.add(playButton);
        panel.add(stopButton);
        panel.add(pickButton);
        panel.add(barChartPanel);
        frame.add(panel);
        frame.setVisible(true);

    }
    private void openFilePicker() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("16-bit .wav files", "wav");
        fileChooser.setFileFilter(filter);
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath();
            System.out.println("Selected file: " + selectedFilePath);
            playing = false;
        }
    }
    private void initMusicPlayer() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        loader = new AudioLoader(selectedFilePath);
        AudioFormat format = loader.getAudioFormat();
        player = new Player(format, RING_BUF_SIZE);
        ringBuffer = player.getRingBuffer();
        lastLoaderBytes = loader.loadToBuffer(ringBuffer, RING_BUF_SIZE);
    }
    private void play(){
        new Thread(() -> {
            while (playing && lastLoaderBytes > 0) {
                try {
                    ringBuffer.applyVolume(0.5);
                    switch (filterMode) {
                        case 0: {
                            ArrayList<Filter> filter = new ArrayList<>();
                            filter.add(lPassIIR.get(filterQuality));
                            ringBuffer.applyFilters(filter);
                        }
                        case 1: {
                            ArrayList<Filter> filter = new ArrayList<>();
                            filter.add(bPassIIR.get(filterQuality));
                            ringBuffer.applyFilters(filter);
                        }
                        case 2: {
                            ArrayList<Filter> filter = new ArrayList<>();
                            filter.add(hPassIIR.get(filterQuality));
                            ringBuffer.applyFilters(filter);
                        }
                        case 3: {
                            ArrayList<Filter> filter = new ArrayList<>();
                            filter.add(lPassFIR.get(filterQuality));
                            ringBuffer.applyFilters(filter);
                        }
                        case 4: {
                            ArrayList<Filter> filter = new ArrayList<>();
                            filter.add(bPassFIR.get(filterQuality));
                            ringBuffer.applyFilters(filter);
                        }
                        case 5: {
                            ArrayList<Filter> filter = new ArrayList<>();
                            filter.add(hPassFIR.get(filterQuality));
                            ringBuffer.applyFilters(filter);
                        }
                    }
                    Runnable fftDisplay = () -> {

                        double[] fftSamples = FFT.toFrequencySpace(DataConverter.byteToShortArrayLIB(ringBuffer.getBuffer()));
                        updateChart(fftSamples);

                        //driveFFTDisplay(ringBuffer.getBuffer());
                    };
                    Thread fftThread = new Thread(fftDisplay);
                    fftThread.setName("FFT");
                    fftThread.start();
                    player.play();
                    lastLoaderBytes = loader.loadToBuffer(ringBuffer, RING_BUF_SIZE);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }
    private void updateChart(double[] samples){
        barChartPanel.setData(samples);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(FilterOrderExperiment::new);
    }
}
