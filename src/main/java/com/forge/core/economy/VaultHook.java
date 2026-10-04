package com.forge.core.economy;

import com.forge.core.ForgeCore;
import org.bukkit.plugin.ServicePriority;

/**
 * Vault integration hook.
 *
 * <p>Registers ForgeCore's economy as a Vault provider, but only if Vault
 * is installed. ForgeCore's
 * built-in economy works standalone; this merely exposes it to other plugins
 * through Vault's API when available.
 *
 * <p>The provider classes are only loaded when Vault is present, so there is
 * no hard dependency.
 */
public final class VaultHook {
    private VaultHook() {
    }

    public static void init(ForgeCore plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                Class.forName("net.milkbowl.vault.economy.Economy");
                plugin.getServer().getServicesManager().register(
                        net.milkbowl.vault.economy.Economy.class,
                        new VaultEconomy(plugin), plugin, ServicePriority.Normal);
                plugin.getLogger().info("Registered Vault economy provider.");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("Vault found but API classes missing.");
            }
        }

    }
}
