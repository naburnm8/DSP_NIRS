package ru.bmstu.naburnm8.dsp.filtering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataConverter {
    public static short[] byteToShortArray(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length - 1; i++) {
            shorts[i] = (short) ((bytes[2 * i] & 0xFF) | (bytes[2 * i + 1] << 8));
        }
        return shorts;
    }
    public static short[] byteToShortArrayLIB(byte[] bytes) {
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
}
