package com.forge.core.cmd.playera;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.Nullable;

/** Case-insensitive enchantment lookup by key (e.g. {@code sharpness}). */
final class Enchants {
    private Enchants() {
    }

    private static Registry<Enchantment> registry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    }

    static @Nullable Enchantment find(String input) {
        String key = input.toLowerCase(Locale.ROOT).replace(' ', '_');
        String namespaced = key.contains(":") ? key : "minecraft:" + key;
        NamespacedKey namespacedKey = NamespacedKey.fromString(namespaced);
        Registry<Enchantment> registry = registry();
        if (namespacedKey != null) {
            Enchantment direct = registry.get(namespacedKey);
            if (direct != null) {
                return direct;
            }
        }
        for (Enchantment enchantment : registry) {
            if (enchantment.getKey().getKey().equalsIgnoreCase(key)) {
                return enchantment;
            }
        }
        return null;
    }

    static List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (Enchantment enchantment : registry()) {
            keys.add(enchantment.getKey().getKey());
        }
        return keys;
    }
}
