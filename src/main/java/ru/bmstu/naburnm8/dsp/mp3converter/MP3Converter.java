package ru.bmstu.naburnm8.dsp.mp3converter;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MP3Converter {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java MP3ToWAVConverter <input.mp3> <output.wav>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(new File(inputFile));
            AudioFormat baseFormat = mp3Stream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
            AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, new File(outputFile));

            System.out.println("Conversion complete: " + outputFile);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Error: Unsupported audio file format.");
        } catch (IOException e) {
            System.err.println("Error: I/O error occurred while processing the file.");
        }
    }
}
