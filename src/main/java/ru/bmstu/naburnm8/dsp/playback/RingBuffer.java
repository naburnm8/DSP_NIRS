package ru.bmstu.naburnm8.dsp.playback;


public class RingBuffer {
    private final byte[] buffer;
    private int writePos = 0;
    private int readPos = 0;
    private int available = 0;
    private double vibratoRate = 0;

    public RingBuffer(int size) {
        buffer = new byte[size];
    }

    public synchronized void write(byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer[writePos] = data[offset + i];
            writePos = (writePos + 1) % buffer.length;
            if (available < buffer.length) {
                available++;
            } else {
                readPos = (readPos + 1) % buffer.length;
            }
        }
    }

    public synchronized int read(byte[] target, int offset, int length) {
        int bytesToRead = Math.min(length, available);
        for (int i = 0; i < bytesToRead; i++) {
            target[offset + i] = buffer[readPos];
            readPos = (readPos + 1) % buffer.length;
        }
        available -= bytesToRead;
        return bytesToRead;
    }

    public synchronized boolean isEmpty() {
        return available == 0;
    }

    public synchronized void applyVolume(double factor) {
        int bufferSize = available;  // Number of samples in the buffer
        int readIndex = readPos;

        // Iterate over the available samples in the buffer
        for (int i = 0; i < bufferSize; i++) {
            short sample = (short)((buffer[readIndex] & 0xFF) | (buffer[(readIndex + 1) % buffer.length] << 8));

            sample = (short)Math.max(Math.min(sample * factor, Short.MAX_VALUE), Short.MIN_VALUE);

            buffer[readIndex] = (byte)(sample & 0xFF);  // Low byte
            buffer[(readIndex + 1) % buffer.length] = (byte)((sample >> 8) & 0xFF);  // High byte
            readIndex = (readIndex + 2) % buffer.length;
        }
    }

    public synchronized void applyEcho(int delayInBytes, double decayFactor) {
        int bufferSize = available;
        int readIndex = readPos;
        for (int i = 0; i < bufferSize; i++) {
            if(i < delayInBytes){
                continue;
            }
            short sample = (short)((buffer[readIndex] & 0xFF) | (buffer[(readIndex + 1) % buffer.length] << 8));

            short delayedSample = (short)((buffer[((bufferSize + readIndex) - (delayInBytes) ) % bufferSize] & 0xFF) | (buffer[((bufferSize + readIndex) - (delayInBytes) + 1) % bufferSize] << 8));
            sample = (short) (sample + delayedSample*decayFactor);

            //sample = (short) (Math.max(Math.min(sample + delayedSample*decayFactor, Short.MAX_VALUE), Short.MAX_VALUE));

            buffer[readIndex] = (byte)(sample & 0xFF);
            buffer[(readIndex + 1) % buffer.length] = (byte)((sample >> 8) & 0xFF);
            readIndex = (readIndex + 2) % buffer.length;
        }
    }

    public synchronized void applyVibrato(double fileRate, double decayFactor, double speedFactor){
        int bufferSize = available;
        int readIndex = readPos;
        if (this.vibratoRate == 0){
            this.vibratoRate = this.buffer.length / (fileRate * 2);
        }
        for (int i = 0; i < bufferSize; i++) {
            short sample = (short)((buffer[readIndex] & 0xFF) | (buffer[(readIndex + 1) % buffer.length] << 8));
            double currentTime = i / (fileRate*2);
            double currentSine = Math.sin(2*Math.PI*currentTime*speedFactor*(1/this.vibratoRate));
            sample = (short)(sample + currentSine*sample*decayFactor);
            buffer[readIndex] = (byte)(sample & 0xFF);
            buffer[(readIndex + 1) % buffer.length] = (byte)((sample >> 8) & 0xFF);
            readIndex = (readIndex + 2) % buffer.length;
        }
    }



    public void applyVolume1Byte(double volume) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte)(buffer[i] * volume);
        }
    }
}

