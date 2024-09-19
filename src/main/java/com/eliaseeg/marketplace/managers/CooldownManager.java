package com.eliaseeg.marketplace.managers;

import com.eliaseeg.marketplace.MarketPlace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long cooldownTime = MarketPlace.getInstance().getConfig().getLong("cooldown.time", 2000);

    public static boolean checkCooldown(UUID playerId) {
        if (cooldowns.containsKey(playerId)) {
            long timeElapsed = System.currentTimeMillis() - cooldowns.get(playerId);
            if (timeElapsed < cooldownTime) {
                return false;
            }
        }
        cooldowns.put(playerId, System.currentTimeMillis());
        return true;
    }

}