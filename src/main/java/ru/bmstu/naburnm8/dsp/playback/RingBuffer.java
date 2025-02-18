package ru.bmstu.naburnm8.dsp.playback;


import ru.bmstu.naburnm8.dsp.filtering.DataConverter;
import ru.bmstu.naburnm8.dsp.filtering.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RingBuffer {
    private final byte[] buffer;
    private final byte[] prevBuffer;
    private int writePos = 0;
    private int readPos = 0;
    private int available = 0;
    private double vibratoRate = 0;
    private final int bufSize;

    public RingBuffer(int size) {
        buffer = new byte[size];
        prevBuffer = new byte[size];
        bufSize = size;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public synchronized void write(byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            prevBuffer[writePos] = buffer[writePos];
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
        int bufferSize = available;  // number of samples in the buffer
        int readIndex = readPos;

        // iterate over the available samples in the buffer
        for (int i = 0; i < bufferSize; i++) {
            short sample = (short)((buffer[readIndex] & 0xFF) | (buffer[(readIndex + 1) % buffer.length] << 8));

            sample = (short)Math.max(Math.min(sample * factor, Short.MAX_VALUE), Short.MIN_VALUE);

            buffer[readIndex] = (byte)(sample & 0xFF);  // low byte
            buffer[(readIndex + 1) % buffer.length] = (byte)((sample >> 8) & 0xFF);  // high byte
            readIndex = (readIndex + 2) % buffer.length; // move the read pos forward
        }
    }

    public synchronized void applyEcho(int delayInBytes, double decayFactor) {
        int bufferSize = available;
        int readIndex = readPos;
        for (int i = 0; i < bufferSize; i++) {
            short sample = (short)((buffer[readIndex] & 0xFF) | (buffer[(readIndex + 1) % buffer.length] << 8));
            if(readIndex < delayInBytes){
                int readOffset = delayInBytes - readIndex;
                short delayedSample = (short)((prevBuffer[((bufferSize) - (readOffset) ) % bufferSize] & 0xFF) | (prevBuffer[((bufferSize) - (readOffset) + 1) % bufferSize] << 8));
                sample = (short) (sample + delayedSample*decayFactor);
                buffer[readIndex] = (byte)(sample & 0xFF);
                buffer[(readIndex + 1) % buffer.length] = (byte)((sample >> 8) & 0xFF);
                readIndex = (readIndex + 2) % buffer.length;
                continue;
            }

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

    public synchronized void applyFilters(List<Filter> filterChain) throws ExecutionException, InterruptedException {
        short[] inputTransformed = DataConverter.byteToShortArrayLIB(buffer);
        short[] filtered = new short[inputTransformed.length];

        int numberOfTasks = filterChain.size();

        for (Filter filter : filterChain) {
            filter.setInputData(inputTransformed);
        }

        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfTasks);
        List<Future<short[]>> futures = executor.invokeAll(filterChain);



        for (Future<short[]> future : futures) {
            short[] result = future.get();
            for (int i = 0; i < filtered.length; i++) {
                filtered[i] += result[i];
            }
        }


        executor.shutdown();


        int bufferSize = available;
        int readIndex = readPos;

        for (int i = 0; i < bufferSize; i++) {
            if (readIndex >= filtered.length) {
                break;
            }
            short sample = filtered[readIndex];
            buffer[readIndex * 2] = (byte) (sample & 0xFF);
            buffer[(readIndex * 2) + 1] = (byte) ((sample >> 8) & 0xFF);
            readIndex = (readIndex + 1) % (filtered.length);
        }
    }

    public void applyVolume1Byte(double volume) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte)(buffer[i] * volume);
        }
    }
}

