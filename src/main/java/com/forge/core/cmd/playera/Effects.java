package com.forge.core.cmd.playera;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.Nullable;

/** Case-insensitive potion effect lookup by key (e.g. {@code speed}). */
final class Effects {
    private Effects() {
    }

    static @Nullable PotionEffectType find(String input) {
        String key = input.toLowerCase(Locale.ROOT).replace(' ', '_');
        String namespaced = key.contains(":") ? key : "minecraft:" + key;
        NamespacedKey namespacedKey = NamespacedKey.fromString(namespaced);
        if (namespacedKey != null) {
            PotionEffectType direct = Registry.POTION_EFFECT_TYPE.get(namespacedKey);
            if (direct != null) {
                return direct;
            }
        }
        for (PotionEffectType type : Registry.POTION_EFFECT_TYPE) {
            if (type.getKey().getKey().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }

    static List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (PotionEffectType type : Registry.POTION_EFFECT_TYPE) {
            keys.add(type.getKey().getKey());
        }
        return keys;
    }
}
