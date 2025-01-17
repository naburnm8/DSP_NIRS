package ru.bmstu.naburnm8.dsp.playback;

import javax.sound.sampled.*;

public class Player {
    private final SourceDataLine sourceDataLine;
    private final RingBuffer ringBuffer;

    public Player(AudioFormat audioFormat, int bufferSize) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException();
        }

        this.sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        this.sourceDataLine.open(audioFormat);
        this.sourceDataLine.start();
        this.ringBuffer = new RingBuffer(bufferSize);
    }

    public void play() {
        byte[] playbackBuf = new byte[1024];
        while(!ringBuffer.isEmpty()){
            int bytesRead = ringBuffer.read(playbackBuf,0, playbackBuf.length);
            sourceDataLine.write(playbackBuf,0, bytesRead);
        }
    }

    public void stop() {
        sourceDataLine.drain();
        sourceDataLine.close();
    }

    public RingBuffer getRingBuffer(){
        return ringBuffer;
    }
}
