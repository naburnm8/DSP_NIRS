package ru.bmstu.naburnm8.dsp.playback;

import ru.bmstu.naburnm8.dsp.files.AudioLoader;

import javax.sound.sampled.AudioFormat;


public class ConsolePlayback {
    public static void main(String[] args){
        String path = "electroswing.wav";
        int ringBufSize = 4096;
        try{
            AudioLoader loader = new AudioLoader(path);
            AudioFormat format = loader.getAudioFormat();
            Player player = new Player(format, ringBufSize);
            RingBuffer ringBuffer = player.getRingBuffer();
            int loaderBytesLoaded = loader.loadToBuffer(ringBuffer, ringBufSize);
            while (loaderBytesLoaded > 0){
                ringBuffer.applyVolume(0.5);
                player.play();
                loaderBytesLoaded = loader.loadToBuffer(ringBuffer, ringBufSize);
            }
            loader.close();
            player.stop();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
