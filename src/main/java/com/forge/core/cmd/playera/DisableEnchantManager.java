package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

/** Server-wide disabled enchantments, persisted in the config. */
final class DisableEnchantManager implements Listener {
    private static final String PATH = "disabled-enchants";
    private final ForgeCore plugin;
    private final Set<String> disabled = ConcurrentHashMap.newKeySet();

    DisableEnchantManager(ForgeCore plugin) {
        this.plugin = plugin;
        reload();
    }

    void reload() {
        disabled.clear();
        disabled.addAll(plugin.getConfig().getStringList(PATH));
    }

    /** Toggle; returns true when the enchantment is now disabled. */
    boolean toggle(Enchantment enchantment) {
        String key = enchantment.getKey().toString();
        boolean nowDisabled = !disabled.contains(key);
        if (nowDisabled) {
            disabled.add(key);
        } else {
            disabled.remove(key);
        }
        List<String> sorted = new ArrayList<>(disabled);
        Collections.sort(sorted);
        plugin.getConfig().set(PATH, sorted);
        plugin.saveConfig();
        return nowDisabled;
    }

    boolean isDisabled(Enchantment enchantment) {
        return disabled.contains(enchantment.getKey().toString());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        for (Enchantment enchantment : event.getEnchantsToAdd().keySet()) {
            if (isDisabled(enchantment)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }
        for (Enchantment enchantment : result.getEnchantments().keySet()) {
            if (isDisabled(enchantment)) {
                event.setResult(null);
                return;
            }
        }
    }
}
