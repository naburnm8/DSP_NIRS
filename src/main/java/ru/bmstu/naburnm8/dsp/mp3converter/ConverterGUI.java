package ru.bmstu.naburnm8.dsp.mp3converter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ConverterGUI extends Component {
    private String selectedFilePath;

    public static void showAlertPopup(String message, Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Attention",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showErrorPopup(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Attention",
                JOptionPane.ERROR_MESSAGE
        );
    }


    public ConverterGUI() {
        JFrame frame = new JFrame("Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 100);
        JButton convert = new JButton("Convert");
        JButton chooseFile = new JButton("Choose File");
        JTextArea textAreaInput = new JTextArea();

        chooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFilePicker();
                textAreaInput.setText(selectedFilePath);
            }
        });

        convert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFilePath == null) {
                    showErrorPopup("Please select a file");
                    return;
                }
                String outputPath = "";
                try{
                    outputPath = selectedFilePath;
                    outputPath = outputPath.substring(0, outputPath.length() - 3) + "wav";
                    MP3Converter.main(new String[]{selectedFilePath, outputPath});
                } catch (Exception ex){
                    showErrorPopup(ex.getMessage());
                    return;
                }
                showAlertPopup("Converted to " + outputPath, frame);
                System.exit(0);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(chooseFile);
        panel.add(convert);
        panel.add(textAreaInput);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void openFilePicker() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".mp3 files", "mp3");
        fileChooser.setFileFilter(filter);
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath();
            System.out.println("Selected file: " + selectedFilePath);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConverterGUI());
    }
}
