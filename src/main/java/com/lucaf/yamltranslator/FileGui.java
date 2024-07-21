/**
 * Apache License 2.0 (Apache-2.0)
 * This file is part of the YamlTranslator project.
 * Author: lucaf
 */

package com.lucaf.yamltranslator;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileGui {
    private JPanel panel1;
    private JButton PICKFILEButton;
    private JTextField inputyFileText;
    private JComboBox fromLang;
    private JComboBox toLang;
    private JButton TRANSLATEButton;
    private JCheckBox ignoreParamFormattingSCheckBox;
    private JCheckBox MINIFONTCheckBox;
    static ExecutorService executorService = Executors.newSingleThreadExecutor();
    public FileGui() {
        ignoreParamFormattingSCheckBox.setSelected(true);
        PICKFILEButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(null);
            inputyFileText.setText(fileChooser.getSelectedFile().getAbsolutePath());
        });
        TRANSLATEButton.addActionListener(e -> {
            GTranslate.Lang from = GTranslate.languages[fromLang.getSelectedIndex()];
            GTranslate.Lang to = GTranslate.languages[toLang.getSelectedIndex()];
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(panel1) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                Translate translator = new Translate(new java.io.File(inputyFileText.getText()),file, from, to);
                translator.setParamSpecialFormatting(ignoreParamFormattingSCheckBox.isSelected());
                translator.setMiniFont(MINIFONTCheckBox.isSelected());
                executorService.submit(translator);
                // save to file
            }

        });
        for (GTranslate.Lang lang : GTranslate.languages) {
            fromLang.addItem(lang.name);
            toLang.addItem(lang.name);
        }
    }

    public void show() {
        JFrame frame = new JFrame("Translator");
        frame.setContentPane(panel1);
        frame.setSize(800, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
