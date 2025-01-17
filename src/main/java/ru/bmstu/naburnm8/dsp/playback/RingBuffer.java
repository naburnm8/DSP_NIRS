package ru.bmstu.naburnm8.dsp.playback;

public class RingBuffer {
    private final byte[] buffer;
    private int writePos = 0;
    private int readPos = 0;
    private int available = 0;

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
}
