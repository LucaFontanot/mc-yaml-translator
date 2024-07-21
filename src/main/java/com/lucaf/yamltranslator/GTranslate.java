/**
 * Apache License 2.0 (Apache-2.0)
 * This file is part of the YamlTranslator project.
 * Author: lucaf
 */

package com.lucaf.yamltranslator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class GTranslate {
    public static class Lang {
        public String code;
        public String name;

        public Lang(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    public static Lang[] languages;

    static {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("langs.json");
        try {
            JSONArray langs = new JSONArray(new String(inputStream.readAllBytes()));
            languages = new Lang[langs.length()];
            for (int i = 0; i < langs.length(); i++) {
                JSONObject lang = langs.getJSONObject(i);
                languages[i] = new Lang(lang.getString("code"), lang.getString("name"));
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildUrl() {
        return "https://translate.google.com/translate_a/single?client=at&dt=t&dt=rm&dj=1";
    }

    private static String buildBody(String text, String from, String to) {
        try {
            text = java.net.URLEncoder.encode(text, "UTF-8");
            from = java.net.URLEncoder.encode(from, "UTF-8");
            to = java.net.URLEncoder.encode(to, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "sl=" + from + "&tl=" + to + "&q=" + text;
    }

    private static Request.Builder getTranslateRequest(String text, String from, String to) {
        return new Request.Builder()
                .url(buildUrl())
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .post(okhttp3.RequestBody.create(buildBody(text, from, to).getBytes()));

    }
    public static String translate_y(String text, String from, String to) throws IOException {
        System.out.println("Translating " + text + " from " + from + " to " + to);
        return text;
    }



    public static String translate(String text, String from, String to) throws IOException {
        System.out.println("Translating " + text + " from " + from + " to " + to);
        OkHttpClient client = new OkHttpClient();
        Request request = getTranslateRequest(text, from, to).build();
        okhttp3.Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        String responseBody = response.body().string();
        System.out.println(responseBody);
        JSONObject json = new JSONObject(responseBody);
        JSONArray sentences = json.getJSONArray("sentences");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < sentences.length(); i++) {
            result.append(sentences.getJSONObject(i).getString("trans"));
        }
        return result.toString();
    }


}
