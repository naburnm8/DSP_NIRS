package ru.bmstu.naburnm8.dsp.filtering;

public class ByteConverter {
    public static short[] byteArrayToShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];
        for (int i = 0; i < shortArray.length; i += 2) {
            shortArray[i] = (short) (((byteArray[2 * i] & 0xFF) << 8) | (byteArray[2 * i + 1] & 0xFF));
        }
        return shortArray;
    }
    public static byte[] shortArrayToByteArray(short[] shortArray) {
        byte[] byteArray = new byte[shortArray.length * 2];

        for (int i = 0; i < shortArray.length; i++) {
            byteArray[2 * i] = (byte) (shortArray[i] >> 8);  // Higher byte
            byteArray[2 * i + 1] = (byte) (shortArray[i]);     // Lower byte
        }

        return byteArray;
    }
    public static int[] byteArrayToIntArray(byte[] byteArray) {
        int length = byteArray.length / 4;  // Since each int is 4 bytes
        int[] intArray = new int[length];

        for (int i = 0; i < length; i++) {
            intArray[i] = (byteArray[4 * i] & 0xFF) << 24 |
                    (byteArray[4 * i + 1] & 0xFF) << 16 |
                    (byteArray[4 * i + 2] & 0xFF) << 8 |
                    (byteArray[4 * i + 3] & 0xFF);
        }

        return intArray;
    }

    public static byte[] intArrayToByteArray(int[] intArray) {
        byte[] byteArray = new byte[intArray.length * 4];  // Each int takes 4 bytes

        for (int i = 0; i < intArray.length; i++) {
            byteArray[4 * i]     = (byte) (intArray[i] >> 24);  // Highest byte
            byteArray[4 * i + 1] = (byte) (intArray[i] >> 16);  // Second byte
            byteArray[4 * i + 2] = (byte) (intArray[i] >> 8);   // Third byte
            byteArray[4 * i + 3] = (byte) (intArray[i]);        // Lowest byte
        }

        return byteArray;
    }
}
