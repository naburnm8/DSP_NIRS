package ru.bmstu.naburnm8.dsp.playback;

import ru.bmstu.naburnm8.dsp.files.AudioLoader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class GUIPlayback extends Component {
    private static final int RING_BUF_SIZE = 131072;

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

    public GUIPlayback() {
        JFrame frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

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

        filePickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFilePicker();
            }
        });

        echoToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                echoActive = !echoActive;
            }
        });

        vibratoToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vibratoActive = !vibratoActive;
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying) {

                    if(selectedFilePath == null) {
                        showErrorPopup("No file selected");
                        return;
                    }
                    try {
                        if(fullStop) {
                            initMusicPlayer();
                            fullStop = false;
                        }
                        isPlaying = true;
                        play();
                    } catch (Exception ex) {
                        showErrorPopup(ex.toString());
                    }

                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying) {
                    isPlaying = false;
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying) {
                    fullStop = true;
                    isPlaying = false;
                }
            }
        });

        volumeSlider.addChangeListener(e -> {
            currentVolume = volumeSlider.getValue() / 100.0f;
        });
        panel.add(filePickButton);
        panel.add(playButton);
        panel.add(pauseButton);
        panel.add(stopButton);
        panel.add(volumeSlider);
        panel.add(echoToggle);
        panel.add(vibratoToggle);

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
        }
    }
    private void play(){
        Runnable runnable = () -> {
          try{
              while (lastLoaderBytes > 0 && isPlaying){
                  ringBuffer.applyVolume(currentVolume);
                  if (echoActive){
                      ringBuffer.applyEcho(8192,0.3);
                  }
                  if (vibratoActive){
                      ringBuffer.applyVibrato(44000, 0.75, 1);
                  }
                  player.play();
                  lastLoaderBytes = loader.loadToBuffer(ringBuffer, RING_BUF_SIZE);
              }
              if (fullStop){
                  player.stop();
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
    }
    private void showErrorPopup(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Attention",
                JOptionPane.ERROR_MESSAGE
        );
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIPlayback());
    }
}
