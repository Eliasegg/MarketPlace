package com.eliaseeg.marketplace.utils;

import com.eliaseeg.marketplace.MarketPlace;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

    private static FileConfiguration config;

    public static void init(MarketPlace plugin) {
        config = plugin.getConfig();
    }

    public static String getMessage(String key, Object... placeholders) {
        String message = config.getString("messages." + key, "Message not found: " + key);
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = "%" + placeholders[i] + "%";
            String value = String.valueOf(placeholders[i + 1]);
            message = message.replace(placeholder, value);
        }
        
        return message;
    }

    public static void sendMessage(Player player, String key, Object... placeholders) {
        player.sendMessage(getMessage(key, placeholders));
    }

    public static List<String> getMessageList(String key, Object... placeholders) {
        List<String> messages = MarketPlace.getInstance().getConfig().getStringList(key);
        return messages.stream()
                .map(message -> {
                    for (int i = 0; i < placeholders.length; i += 2) {
                        String placeholder = "%" + placeholders[i] + "%";
                        String value = String.valueOf(placeholders[i + 1]);
                        message = message.replace(placeholder, value);
                    }
                    return ChatColor.translateAlternateColorCodes('&', message);
                })
                .collect(Collectors.toList());
    }
}