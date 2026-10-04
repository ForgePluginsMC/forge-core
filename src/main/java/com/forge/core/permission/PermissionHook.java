package com.forge.core.permission;

import com.forge.core.ForgeCore;
import org.bukkit.plugin.ServicePriority;

/**
 * Registers ForgeCore's permission system as the Vault permission provider.
 * The Vault API is bundled in this jar, so no standalone Vault plugin is
 * needed — other plugins querying Vault's Permission service find ForgeCore.
 */
public final class PermissionHook {
    private PermissionHook() {
    }

    public static void init(ForgeCore plugin) {
        plugin.getServer().getServicesManager().register(
                net.milkbowl.vault.permission.Permission.class,
                new VaultPermission(plugin), plugin, ServicePriority.Normal);
        plugin.getLogger().info("Registered Vault permission provider.");
    }
}
