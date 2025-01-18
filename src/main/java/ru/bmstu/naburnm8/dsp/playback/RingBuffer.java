package ru.bmstu.naburnm8.dsp.playback;


public class RingBuffer {
    private byte[] buffer;
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

    public synchronized void applyVolume(double factor) {
        int bufferSize = available;  // Number of samples in the buffer
        int readIndex = readPos;

        // Iterate over the available samples in the buffer
        for (int i = 0; i < bufferSize; i++) {
            // Read the 16-bit sample (two bytes per sample)
            short sample = (short)((buffer[readIndex] & 0xFF) | (buffer[(readIndex + 1) % buffer.length] << 8));

            // Modify the sample by the factor
            sample = (short)Math.max(Math.min(sample * factor, Short.MAX_VALUE), Short.MIN_VALUE);

            // Write the modified sample back into the buffer (two bytes)
            buffer[readIndex] = (byte)(sample & 0xFF);  // Low byte
            buffer[(readIndex + 1) % buffer.length] = (byte)((sample >> 8) & 0xFF);  // High byte

            // Move to the next sample (advance by 2 bytes)
            readIndex = (readIndex + 2) % buffer.length;
        }
    }

    public void applyVolume1Byte(double volume) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte)(buffer[i] * volume);
        }
    }
}
