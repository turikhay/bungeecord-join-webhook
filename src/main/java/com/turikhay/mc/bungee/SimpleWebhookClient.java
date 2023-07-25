package com.turikhay.mc.bungee;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Level;

@Log
public class SimpleWebhookClient implements WebhookClient {
    private final URL webhookUrl;

    public SimpleWebhookClient(URL webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void postEvent(String name, Map<String, String> data) {
        try {
            doPostEvent(name, data);
        } catch (Exception e) {
            log.log(Level.WARNING, "failure posting an event", e);
        }
    }

    private void doPostEvent(String name, Map<String, String> data) throws Exception {
        HttpURLConnection c = (HttpURLConnection) webhookUrl.openConnection();
        try {
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setUseCaches(false);
            c.setDoOutput(true);
            try (OutputStreamWriter w = new OutputStreamWriter(c.getOutputStream(), StandardCharsets.UTF_8)) {
                w.write("event=");
                w.write(encode(name));
                w.write("&timestamp=");
                w.write(encode(Instant.now().toString()));
                for (Map.Entry<String, String> e : data.entrySet()) {
                    w.write("&");
                    w.write(encode(e.getKey()));
                    w.write("=");
                    w.write(encode(e.getValue()));
                }
            }
            int response = c.getResponseCode();
            if (response < 200 || response > 299) {
                throw new IOException("invalid response code: " + response);
            }
        } finally {
            c.disconnect();
        }
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encode(String v) {
        return URLEncoder.encode(v, "UTF-8");
    }

    @Override
    public void cleanUp() {
    }
}
