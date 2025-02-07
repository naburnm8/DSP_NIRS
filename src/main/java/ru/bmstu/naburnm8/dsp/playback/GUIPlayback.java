package ru.bmstu.naburnm8.dsp.playback;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import ru.bmstu.naburnm8.dsp.files.AudioLoader;
import ru.bmstu.naburnm8.dsp.filtering.BarChartPanel;
import ru.bmstu.naburnm8.dsp.filtering.DataConverter;
import ru.bmstu.naburnm8.dsp.filtering.FFT;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GUIPlayback extends Component {
    private static final int RING_BUF_SIZE = 65536;
    private static final String label = "Music Player, now playing: ";
    private static final String staticLabel = "Music Player";

    private Player player;
    private RingBuffer ringBuffer;
    private AudioLoader loader;
    private boolean isPlaying = false;
    private boolean fullStop = true;
    private float currentVolume = 0.5f;
    private boolean echoActive = false;
    private boolean vibratoActive = false;
    private String selectedFilePath;
    private int lastLoaderBytes;
    private float musicRate;
    private final ArrayList<Integer> bandLevels; // will be used for filters
    private final ArrayList<JLabel> bandLabels;

    private double echoIntensity = 0.3;
    private int echoDepth = 8192;
    private double vibratoDecay = 0.75;
    private double vibratoSpeed = 1;

    private final BarChartPanel barChartPanel;

    public GUIPlayback() {
        JFrame frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2048, 1024);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        double[] initialData = {10, 20, 30, 40, 50};
        barChartPanel = new BarChartPanel(initialData, Color.BLUE, "FFT", 750);


        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");
        JButton filePickButton = new JButton("Pick a .wav file");
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        JToggleButton echoToggle = new JToggleButton("Echo");
        JToggleButton vibratoToggle = new JToggleButton("Vibrato");

        JPanel bandPanel = new JPanel();
        bandPanel.setLayout(new BoxLayout(bandPanel, BoxLayout.Y_AXIS));
        bandLevels = new ArrayList<>();
        bandLabels = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            JSlider newSlider = new JSlider(-60, 0, 0);
            newSlider.setMajorTickSpacing(10);
            newSlider.setPaintTicks(true);
            newSlider.setPaintLabels(true);
            JLabel bandLabel = new JLabel("Band " + (i+1) + " level");

            int finalI = i;
            newSlider.addChangeListener(e -> {
                changeBandLevel(finalI, newSlider.getValue());
            });
            JPanel sliderPanel = new JPanel();
            sliderPanel.setLayout(new FlowLayout());
            sliderPanel.add(newSlider);
            JLabel indicator = new JLabel("0");
            sliderPanel.add(indicator);
            bandLabels.add(indicator);
            bandPanel.add(bandLabel);
            bandPanel.add(sliderPanel);
            bandLevels.add(0);
        }

        JSlider echoIntensity = new JSlider(10, 100, 30);
        JSlider echoDepth = new JSlider(0,16384, 4096); // in samples
        JSlider vibratoDecay = new JSlider(0, 100, 75);
        JSlider vibratoSpeed = new JSlider(0, 300, 100);

        echoIntensity.setMajorTickSpacing(10);
        echoDepth.setMajorTickSpacing(4096);
        echoIntensity.setPaintTicks(true);
        echoIntensity.setPaintLabels(true);
        echoDepth.setPaintLabels(true);
        echoDepth.setPaintTicks(true);

        vibratoDecay.setMajorTickSpacing(25);
        vibratoDecay.setPaintTicks(true);
        vibratoDecay.setPaintLabels(true);

        vibratoSpeed.setMajorTickSpacing(50);
        vibratoSpeed.setPaintTicks(true);
        vibratoSpeed.setPaintLabels(true);

        echoIntensity.addChangeListener(e -> this.echoIntensity = (double) echoIntensity.getValue() / 100);

        echoDepth.addChangeListener(e -> this.echoDepth = echoDepth.getValue() * 2);

        vibratoDecay.addChangeListener(e -> this.vibratoDecay = (double) vibratoDecay.getValue() / 100);

        vibratoSpeed.addChangeListener(e -> this.vibratoSpeed = (double) vibratoSpeed.getValue() / 100);

        JPanel effectsPanel = new JPanel();
        effectsPanel.setLayout(new BoxLayout(effectsPanel, BoxLayout.PAGE_AXIS));


        JLabel echoIntText = new JLabel("Echo intensity, %");
        JLabel echoDepthText = new JLabel("Echo depth, samples");
        JLabel vibratoDecayText = new JLabel("Vibrato decay, %");
        JLabel vibratoSpeedText = new JLabel("Vibrato speed, %");

        JPanel effectButtons = new JPanel();
        effectButtons.setLayout(new BoxLayout(effectButtons, BoxLayout.X_AXIS));
        effectButtons.setSize(200, 50);
        effectButtons.add(echoToggle);
        effectButtons.add(vibratoToggle);

        effectsPanel.add(effectButtons);
        effectsPanel.add(echoIntensity);
        effectsPanel.add(echoIntText);
        effectsPanel.add(echoDepth);
        effectsPanel.add(echoDepthText);
        effectsPanel.add(vibratoDecay);
        effectsPanel.add(vibratoDecayText);
        effectsPanel.add(vibratoSpeed);
        effectsPanel.add(vibratoSpeedText);



        filePickButton.addActionListener(e -> openFilePicker());

        echoToggle.addActionListener(e -> echoActive = !echoActive);

        vibratoToggle.addActionListener(e -> vibratoActive = !vibratoActive);

        playButton.addActionListener(e -> {
            if (!isPlaying) {

                if(selectedFilePath == null) {
                    showErrorPopup("No file selected");
                    return;
                }
                try {
                    if(fullStop) {
                        initMusicPlayer();
                        if(loader.getAudioFormat().getSampleSizeInBits() != 16){
                            showErrorPopup(".wav file must be 16-bit!");
                            return;
                        }
                        fullStop = false;
                    }
                    String fileName = selectedFilePath.split(File.pathSeparator)[selectedFilePath.split(File.pathSeparator).length - 1];
                    frame.setTitle(label + fileName);
                    isPlaying = true;
                    play();
                } catch (Exception ex) {
                    showErrorPopup(ex.toString());
                }

            }
        });

        pauseButton.addActionListener(e -> {
            if (isPlaying) {
                isPlaying = false;
                String fileName = selectedFilePath.split(File.pathSeparator)[selectedFilePath.split(File.pathSeparator).length - 1];
                frame.setTitle(label + fileName + " paused");
            }
        });

        stopButton.addActionListener(e -> {
            if (isPlaying) {
                fullStop = true;
                isPlaying = false;
                frame.setTitle(staticLabel);
                return;
            }
            fullStop = true;
            frame.setTitle(staticLabel);
        });

        volumeSlider.addChangeListener(e -> currentVolume = volumeSlider.getValue() / 100.0f);
        panel.add(filePickButton);
        panel.add(playButton);
        panel.add(pauseButton);
        panel.add(stopButton);
        panel.add(volumeSlider);


        mainPanel.add(panel, BorderLayout.NORTH);
        mainPanel.add(barChartPanel, BorderLayout.CENTER);
        mainPanel.add(bandPanel, BorderLayout.EAST);
        mainPanel.add(effectsPanel, BorderLayout.WEST);

        frame.add(mainPanel);
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
            fullStop = true;
        }
    }
    private void play(){
        Runnable runnable = () -> {
          try{
              while (lastLoaderBytes > 0 && isPlaying){
                  ringBuffer.applyVolume(currentVolume);
                  if (echoActive){
                      ringBuffer.applyEcho(echoDepth, echoIntensity);
                  }
                  if (vibratoActive){
                      ringBuffer.applyVibrato(musicRate, vibratoDecay, vibratoSpeed);
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
              }
              if (fullStop){
                  player.stop();
              }
              if (isPlaying){
                  fullStop = true;
                  isPlaying = false;
              }
          } catch (Exception e){
              e.printStackTrace();
          }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    private void initMusicPlayer() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        loader = new AudioLoader(selectedFilePath);
        AudioFormat format = loader.getAudioFormat();
        player = new Player(format, RING_BUF_SIZE);
        ringBuffer = player.getRingBuffer();
        lastLoaderBytes = loader.loadToBuffer(ringBuffer, RING_BUF_SIZE);
        musicRate = format.getSampleRate();
    }
    private void showErrorPopup(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Attention",
                JOptionPane.ERROR_MESSAGE
        );
    }
    private void updateChart(double[] fftSamples) {
        barChartPanel.setData(fftSamples);
    }

    private void driveFFTDisplay(byte[] bytes) {  // TODO: FIX FFT UPDATES
        // assume FFT output divided by 4
        int n = RING_BUF_SIZE / 2;
        n = n / 2; //screen updates 10 times per buffer
        System.out.println(n);
        ArrayList<byte[]> chunks = new ArrayList<>();
        byte[] currentChunk = new byte[n];
        for (int i = 0; i < bytes.length; i++) {
            if (i % n == 0 && i != 0) {
                chunks.add(currentChunk);
                currentChunk = new byte[n];
            }
            else{
                currentChunk[i % n] = bytes[i];
            }
        }
        System.out.println(chunks.size());

        for (byte[] chunk : chunks) {
            try {
                int currentSleepRate = (int) (((loader.getAudioFormat().getSampleRate() / RING_BUF_SIZE) / 2) * 1000);
                Thread.sleep(currentSleepRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double[] fftSamples = FFT.toFrequencySpace(DataConverter.byteToShortArrayLIB(chunk));
            updateChart(fftSamples);
        }
    }
    private void changeBandLevel(int i, int bandLevel){
        bandLevels.set(i, bandLevel);
        bandLabels.get(i).setText(bandLevel + "");
        //System.out.println(bandLevels);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIPlayback::new);
    }
}
