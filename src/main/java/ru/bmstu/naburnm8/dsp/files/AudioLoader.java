package ru.bmstu.naburnm8.dsp.files;

import ru.bmstu.naburnm8.dsp.playback.RingBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class AudioLoader {
    private final AudioInputStream audioInputStream;

    public AudioLoader(String path) throws UnsupportedAudioFileException, IOException {
        File audioFile = new File(path);
        this.audioInputStream = AudioSystem.getAudioInputStream(audioFile);
    }

    public AudioFormat getAudioFormat() {
        return audioInputStream.getFormat();
    }

    public int loadToBuffer(RingBuffer ringBuffer, int bufferSize) throws IOException {
        byte[] temp = new byte[bufferSize];
        int bytesRead = audioInputStream.read(temp, 0, bufferSize);
        if (bytesRead > 0) {
            ringBuffer.write(temp, 0, bytesRead);
        }
        return bytesRead;
    }
    public void close() throws IOException {
        audioInputStream.close();
    }
}
