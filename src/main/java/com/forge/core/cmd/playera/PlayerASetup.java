package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 * Wires pack-owned managers, listeners and the playtime ticker.
 * Called once at the top of {@link PlayerAPack#commands}.
 */
public final class PlayerASetup {
    private static boolean initialized;

    private PlayerASetup() {
    }

    /** Register listeners and start the playtime ticker (idempotent). */
    public static void init(ForgeCore plugin) {
        if (initialized) {
            return;
        }
        initialized = true;

        TmbManager tmb = new TmbManager(plugin);
        CuffManager cuff = new CuffManager();
        DisableEnchantManager disabledEnchants = new DisableEnchantManager(plugin);
        PlayerAState.tmb = tmb;
        PlayerAState.cuff = cuff;
        PlayerAState.disabledEnchants = disabledEnchants;

        PluginManager manager = plugin.getServer().getPluginManager();
        manager.registerEvents(new PlayerAListener(plugin, tmb), plugin);
        manager.registerEvents(tmb, plugin);
        manager.registerEvents(cuff, plugin);
        manager.registerEvents(disabledEnchants, plugin);
        manager.registerEvents(new TotemListener(plugin), plugin);
        manager.registerEvents(new ShiftEditListener(plugin), plugin);

        // Playtime ticker: +60 seconds for everyone online, every 60 seconds.
        plugin.getServer().getScheduler().runTaskTimer(plugin,
                task -> {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        plugin.users().get(player).addPlaytime(60);
                    }
                }, 1200L, 1200L);
    }
}
