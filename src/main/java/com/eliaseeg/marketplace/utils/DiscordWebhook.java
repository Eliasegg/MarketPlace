package com.eliaseeg.marketplace.utils;

import com.eliaseeg.marketplace.MarketPlace;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendMessage(String content) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketPlace.getInstance(), () -> {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("content", content);

                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getInputStream().close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}