package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.Nullable;

/**
 * Totem auto-use from anywhere in the inventory. When fatal damage lands on a
 * flagged player who carries a totem anywhere, one totem is consumed, the
 * damage is cancelled and the vanilla totem effects are applied.
 */
final class TotemListener implements Listener {
    private final ForgeCore plugin;

    TotemListener(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.users().get(player).getBoolean("totem", false)) {
            return;
        }
        if (event.getFinalDamage() < player.getHealth()) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        int slot = findTotem(inventory);
        if (slot < 0) {
            return;
        }
        var stack = inventory.getItem(slot);
        if (stack == null) {
            return;
        }
        if (stack.getAmount() <= 1) {
            inventory.setItem(slot, null);
        } else {
            stack.setAmount(stack.getAmount() - 1);
        }
        event.setCancelled(true);
        player.setHealth(1.0);
        player.setFireTicks(0);
        for (PotionEffect active : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(active.getType());
        }
        add(player, "regeneration", 800, 1);
        add(player, "absorption", 100, 1);
        add(player, "fire_resistance", 800, 0);
        Location location = player.getLocation();
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, location.clone().add(0, 1, 0), 40, 0.5, 0.5, 0.5);
        player.getWorld().playSound(location, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
    }

    private static int findTotem(PlayerInventory inventory) {
        var contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            var item = contents[i];
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private static void add(Player player, String key, int ticks, int amplifier) {
        @Nullable PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(key));
        if (type != null) {
            player.addPotionEffect(new PotionEffect(type, ticks, amplifier));
        }
    }
}
