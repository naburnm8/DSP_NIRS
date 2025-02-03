package ru.bmstu.naburnm8.dsp.playback;

import ru.bmstu.naburnm8.dsp.files.AudioLoader;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIPlayback {
    private Player player;
    private RingBuffer ringBuffer;
    private AudioLoader loader;
    private boolean isPlaying = false;
    private float currentVolume = 0.5f;

    public GUIPlayback() {
        JFrame frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying) {
                    playMusic();
                    isPlaying = true;
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
                    player.stop();
                    isPlaying = false;
                }
            }
        });

        volumeSlider.addChangeListener(e -> {
            currentVolume = volumeSlider.getValue() / 100.0f;
        });

        panel.add(playButton);
        panel.add(pauseButton);
        panel.add(stopButton);
        panel.add(volumeSlider);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void playMusic() {
        String path = "electroswing.wav";
        int ringBufSize = 131072;
        Runnable runnable = () -> {
            try{
                AudioLoader loader = new AudioLoader(path);
                AudioFormat format = loader.getAudioFormat();
                Player player = new Player(format, ringBufSize);
                RingBuffer ringBuffer = player.getRingBuffer();
                int loaderBytesLoaded = loader.loadToBuffer(ringBuffer, ringBufSize);
                while (loaderBytesLoaded > 0 && isPlaying){
                    ringBuffer.applyVolume(currentVolume);
                    ringBuffer.applyEcho(4096,0.3);
                    //ringBuffer.applyVibrato(44000, 0.75, 1);
                    player.play();
                    loaderBytesLoaded = loader.loadToBuffer(ringBuffer, ringBufSize);
                }
                loader.close();
                player.stop();
            } catch (Exception e){
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIPlayback());
    }
}
