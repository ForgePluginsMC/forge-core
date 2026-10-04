package com.forge.core.cmd.systemsb.flight;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Flight charges: a spendable balance that powers flight. Flying players
 * (survival/adventure, without {@code forgecore.flightcharge.bypass}) drain
 * {@code flightcharge.drain-per-minute} (default 60) every second; at zero
 * charges flight is revoked.
 */
public final class FlightChargeManager {
    private final ForgeCore plugin;

    public FlightChargeManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> drain(), 20L, 20L);
    }

    /** Maximum charges ({@code flightcharge.max}, default 3600). */
    public double max() {
        return Math.max(1.0, plugin.getConfig().getDouble("flightcharge.max", 3600.0));
    }

    public double get(UUID uuid) {
        return plugin.users().get(uuid).getDouble("flight-charges", 0.0);
    }

    public void set(UUID uuid, double amount) {
        double clamped = Math.min(max(), Math.max(0.0, amount));
        plugin.users().get(uuid).setDouble("flight-charges", clamped);
        plugin.users().save(uuid);
    }

    public void add(UUID uuid, double amount) {
        set(uuid, get(uuid) + amount);
    }

    /** Whole-number display of a player's charges. */
    public String display(UUID uuid) {
        return String.format(Locale.ROOT, "%,.0f", get(uuid));
    }

    private void drain() {
        double perMinute = plugin.getConfig().getDouble("flightcharge.drain-per-minute", 60.0);
        if (perMinute <= 0) {
            return;
        }
        double perSecond = perMinute / 60.0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isFlying()) {
                continue;
            }
            GameMode mode = player.getGameMode();
            if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
                continue;
            }
            if (player.hasPermission("forgecore.flightcharge.bypass")) {
                continue;
            }
            double left = get(player.getUniqueId()) - perSecond;
            if (left <= 0.0) {
                set(player.getUniqueId(), 0.0);
                player.setFlying(false);
                player.setAllowFlight(false);
                Text.error(player, "Your flight charges ran out!");
            } else {
                set(player.getUniqueId(), left);
            }
        }
    }
}
