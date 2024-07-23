/**
 * Apache License 2.0 (Apache-2.0)
 * This file is part of the YamlTranslator project.
 * Author: lucaf
 */

package com.lucaf.yamltranslator;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class Translate implements Callable<Yaml> {

    public static Map<String, String> fonts = Map.of(
            "classic", "qwertyuiopasdfghjklzxcvbnm",
            "slim", "ǫᴡᴇʀᴛʏᴜɪᴏᴘᴀsᴅғɢʜᴊᴋʟᴢxᴄᴠʙɴᴍ"
    );

    final File file;
    final File output;
    final DuckDuckTranslate.Lang from;
    final DuckDuckTranslate.Lang to;
    private JLabel translatorTitle;
    private JTextArea logger;
    private JPanel panel;


    public Translate(File file, File output, DuckDuckTranslate.Lang from, DuckDuckTranslate.Lang to) {
        this.file = file;
        this.from = from;
        this.to = to;
        this.output = output;
    }

    boolean paramSpecialFormatting = false;

    public void setParamSpecialFormatting(boolean paramSpecialFormatting) {
        this.paramSpecialFormatting = paramSpecialFormatting;
    }

    boolean miniFont = false;

    public void setMiniFont(boolean miniFont) {
        this.miniFont = miniFont;
    }

    public static Map<String, String> formatCodes = Map.of(
            "<", ">",
            "/", " "
    );

    public static Map<String, String> specialFormats = Map.of(
            "%", " ",
            "[", "]",
            "{", "}",
            "#", " "
    );

    public static boolean arrayIncludes(String[] arr, String str) {
        for (String s : arr) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldReplace(String str) {
        //remove all special chars from string
        String str2 = str.replaceAll("[^a-zA-Z0-9]", "");
        return !str2.isEmpty();
    }

    public String translateMcFormattedString(String tr) {
        setProgress(progress);
        Map<String, String> formats = new HashMap<>();
        for (String key : formatCodes.keySet()) {
            formats.put(key, formatCodes.get(key));
        }
        if (paramSpecialFormatting) {
            for (String key : specialFormats.keySet()) {
                formats.put(key, specialFormats.get(key));
            }
        }
        String[] arrs = new String[formats.size()];
        formats.keySet().toArray(arrs);
        int formatOpen = -1;
        String closeFormat = "";
        StringBuilder translated = new StringBuilder();
        for (int i = 0; i < tr.length(); i++) {
            if (arrayIncludes(arrs, String.valueOf(tr.charAt(i)))) {
                if (formatOpen == -1) {
                    if (translated.length() > 0) {
                        String newStr = replaceString(tr, i, translated.toString());
                        i = i - tr.length() + newStr.length();
                        translated = new StringBuilder();
                        tr = newStr;
                    }
                    formatOpen = i;
                    closeFormat = formats.get(String.valueOf(tr.charAt(i)));
                }
            }
            if (tr.charAt(i) == '§' || tr.charAt(i) == '&') {
                if (translated.length() > 0) {
                    String newStr = replaceString(tr, i, translated.toString());
                    i = i - tr.length() + newStr.length();
                    translated = new StringBuilder();
                    tr = newStr;
                }
                i++;
                continue;
            }
            if (formatOpen != -1) {
                if (tr.charAt(i) == closeFormat.charAt(0)) {
                    formatOpen = -1;
                }
                continue;
            }
            translated.append(tr.charAt(i));
        }
        tr = replaceString(tr, tr.length(), translated.toString());
        return tr;
    }

    int count = 0;

    public void countStrings(Map<String, Object> obj) {
        for (String key : obj.keySet()) {
            if (obj.get(key) instanceof String) {
                count++;
            }
            if (obj.get(key) instanceof Map) {
                countStrings((Map<String, Object>) obj.get(key));
            }
            if (obj.get(key) instanceof List) {
                for (Object o : (List) obj.get(key)) {
                    if (o instanceof Map) {
                        countStrings((Map<String, Object>) o);
                    }
                    if (o instanceof String) {
                        count++;
                    }
                }
            }
        }
    }

    String replaceString(String str, int i, String s) {
        if (shouldReplace(s.toString())) {
            try {

                String translated_g = s;
                if (!from.code.equals(to.code)) {
                    translated_g = DuckDuckTranslate.translate(s, from.code, to.code);
                }
                if (miniFont) {
                    StringBuilder translated_g_mini = new StringBuilder();
                    translated_g = translated_g.toLowerCase();
                    String base = fonts.get("classic");
                    String base2 = fonts.get("slim");
                    for (int j = 0; j < translated_g.length(); j++) {
                        if (base.contains(String.valueOf(translated_g.charAt(j)))) {
                            char original = translated_g.charAt(j);
                            char replaced = base2.charAt(base.indexOf(original));
                            translated_g_mini.append(replaced);
                        } else {
                            translated_g_mini.append(translated_g.charAt(j));
                        }
                    }
                    translated_g = translated_g_mini.toString();
                }
                if (s.startsWith(" ") && !translated_g.startsWith(" ")) {
                    translated_g = " " + translated_g;
                }
                if (s.endsWith(" ") && !translated_g.endsWith(" ")) {
                    translated_g = translated_g + " ";
                }
                if (s.endsWith("\n") && !translated_g.endsWith("\n")) {
                    translated_g = translated_g + "\n";
                }
                if (s.startsWith("\n") && !translated_g.startsWith("\n")) {
                    translated_g = "\n" + translated_g;
                }
                return str.substring(0, i - s.length()) + translated_g + str.substring(i);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return str;
    }

    int progress = 0;

    public void findStrings(Map<String, Object> obj) {
        for (String key : obj.keySet()) {
            if (obj.get(key) instanceof String) {
                progress++;
                addLog("Translating: " + obj.get(key).toString());
                obj.put(key, translateMcFormattedString(obj.get(key).toString()));
                addLog("Translated: " + obj.get(key).toString());
            }
            if (obj.get(key) instanceof Map) {
                findStrings((Map<String, Object>) obj.get(key));
            }
            if (obj.get(key) instanceof List) {
                int i = 0;
                List onn = (List) obj.get(key);
                for (Object o : onn) {
                    if (o instanceof Map) {
                        findStrings((Map<String, Object>) o);
                    }
                    if (o instanceof String) {
                        progress++;
                        addLog("Translating: " + o.toString());
                        onn.set(i, translateMcFormattedString(o.toString()));
                        //obj.put(key, translateMcFormattedString(o.toString()));
                        addLog("Translated: " + onn.get(i).toString());
                    }
                    i++;
                }
            }
        }
    }

    public void setProgress(int progress) {
        translatorTitle.setText("Translate: " + progress + "/" + count);
    }

    public void addLog(String log) {
        logger.append(log + "\n");
        //scroll to bottom
        logger.setCaretPosition(logger.getDocument().getLength());
    }

    @Override
    public Yaml call() throws Exception{
        JFrame frame = new JFrame("Translate job");
        try {
            FileInputStream inputStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            Map<String, Object> obj = yaml.load(inputStream);
            countStrings(obj);
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 300);
            frame.setVisible(true);
            frame.setResizable(false);
            //new inline thread
            Thread thread = new Thread(() -> {
                findStrings(obj);
                try {
                    DumperOptions options = new DumperOptions();
                    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    options.setPrettyFlow(true);
                    Yaml yamlOut = new Yaml(options);
                    yamlOut.dump(obj, new FileWriter(output));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                frame.dispose();
            });
            thread.start();
            return null;
        } catch (Exception e) {
            //Send alert
            JOptionPane.showMessageDialog(null, "Error, the file is not a valid YAML or is not readable:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
        }
        return null;
    }
}
