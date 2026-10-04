package com.forge.core.economy;

import com.forge.core.ForgeCore;
import org.bukkit.plugin.ServicePriority;

/**
 * Vault integration hook.
 *
 * <p>ForgeCore IS the Vault economy provider — the Vault API is bundled in
 * this jar, and the economy service is always registered. Other plugins
 * looking for Vault's Economy service will find ForgeCore, with or without
 * the standalone Vault plugin installed.
 */
public final class VaultHook {
    private VaultHook() {
    }

    public static void init(ForgeCore plugin) {
        plugin.getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                new VaultEconomy(plugin), plugin, ServicePriority.Normal);
        plugin.getLogger().info("Registered Vault economy provider.");
    }
}
