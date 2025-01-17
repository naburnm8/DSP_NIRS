package ru.bmstu.naburnm8.dsp.playback;

import ru.bmstu.naburnm8.dsp.files.AudioLoader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class ConsolePlayback {
    public static void main(String[] args){
        String path = "electroswing.wav";
        int ringBufSize = 4096;
        try {
            AudioLoader loader = new AudioLoader(path);
            AudioFormat format = loader.getAudioFormat();

            Player player = new Player(format, ringBufSize);
            RingBuffer ringBuffer = player.getRingBuffer();
            Thread loaderThread = new Thread(() -> {
               try {
                   int bytesRead;
                   do {
                       bytesRead = loader.loadToBuffer(ringBuffer, 1024);
                   } while (bytesRead > 0);
               } catch (IOException e) {
                   e.printStackTrace();
               }
            });
            Thread playerThread = new Thread(player::play);

            loaderThread.start();
            playerThread.start();

            loaderThread.join();
            playerThread.join();

            loader.close();
            player.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
