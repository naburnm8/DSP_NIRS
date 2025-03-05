package ru.bmstu.naburnm8.dsp.experiments;

import ru.bmstu.naburnm8.dsp.files.AudioLoader;
import ru.bmstu.naburnm8.dsp.files.FilterParser;
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

public class BufferSizeExperiment extends Component {
    private static final int RING_BUF_SIZE = 131072;
    private double currentVolume = 0.5;
    private boolean playing = false;
    private String selectedFilePath;
    private Player player;
    private RingBuffer ringBuffer;
    private AudioLoader loader;
    private int lastLoaderBytes;



    public BufferSizeExperiment() {
        JFrame frame = new JFrame("Buffer Size Experiment");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,200);
        JButton playButton = new JButton("Play");
        JButton stopButton = new JButton("Stop");
        JButton pickButton = new JButton("Pick Wav");
        JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setMinorTickSpacing(1);

        this.lines = getLines();


        volumeSlider.setPaintTicks(true);

        volumeSlider.addChangeListener(e -> {
           currentVolume = volumeSlider.getValue() / 100.0;
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

        JLabel ringBufSizeLabel = new JLabel(String.valueOf(RING_BUF_SIZE/2));

        panel.add(playButton);
        panel.add(stopButton);
        panel.add(volumeSlider);
        panel.add(pickButton);
        panel.add(ringBufSizeLabel);

        frame.add(panel);

        frame.setVisible(true);
    }

    private void play(){
        new Thread(() -> {
            while (playing && lastLoaderBytes > 0) {
                try {
                    ringBuffer.applyVolume(currentVolume);
                    ringBuffer.notifyBufferReady(lines);
                    player.play();
                    lastLoaderBytes = loader.loadToBuffer(ringBuffer, RING_BUF_SIZE);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }).start();
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

    private ArrayList<Filter> lines;
    private ArrayList<Filter> getLines(){
        return new ArrayList<>(FilterParser.parseFilters(true, false));
    }
    private void initMusicPlayer() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        loader = new AudioLoader(selectedFilePath);
        AudioFormat format = loader.getAudioFormat();
        player = new Player(format, RING_BUF_SIZE);
        ringBuffer = player.getRingBuffer();
        lastLoaderBytes = loader.loadToBuffer(ringBuffer, RING_BUF_SIZE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BufferSizeExperiment::new);
    }
}
