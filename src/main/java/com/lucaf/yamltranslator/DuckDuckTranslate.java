/**
 * Apache License 2.0 (Apache-2.0)
 * This file is part of the YamlTranslator project.
 * Author: lucaf
 */

package com.lucaf.yamltranslator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DuckDuckTranslate {
    private static String vqd = "";
    private static String UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) GSA/125.2.332137730 Mobile/17H35 Safari/604.1";

    public static void setVqd() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://duckduckgo.com/?t=lm&q=translate&ia=web")
                .addHeader("User-Agent", UA)
                .get().build();
        okhttp3.Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        String responseBody = response.body().string();
        String regex = "vqd=\"(.*?)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            vqd = matcher.group(1);
        }
    }

    private static String buildUrl(String from, String to) {
        return "https://duckduckgo.com/translation.js?vqd="+vqd+"&query=translate&from="+from+"&to="+to;
    }
    static int count = 0;
    public static String translate(String text, String from, String to) throws IOException, InterruptedException {
        if (vqd.isEmpty()){
            setVqd();
        }
        count++;
        Thread.sleep(300);
        if (count > 20){
            setVqd();
            count = 0;
        }
        System.out.println("Translating " + text + " from " + from + " to " + to);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(buildUrl(from, to))
                .addHeader("User-Agent", UA)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(text.getBytes())).build();
        okhttp3.Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.err.println("Unexpected code " + response);
            return text;
        }
        String responseBody = response.body().string();
        JSONObject json = new JSONObject(responseBody);
        System.out.println("Translated " + text + " from " + from + " to " + to + " to " + json.getString("translated"));
        return json.getString("translated");
    }
}
