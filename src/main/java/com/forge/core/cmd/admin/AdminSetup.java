package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;

/**
 * Wires up the admin pack: shared {@link InventoryStore} plus listeners.
 * Called once from {@link AdminPack#commands(ForgeCore)}.
 */
public final class AdminSetup {
    private static InventoryStore inventories;
    private static boolean initialized;

    private AdminSetup() {
    }

    public static void init(ForgeCore plugin) {
        if (initialized) {
            return;
        }
        initialized = true;
        inventories = new InventoryStore(plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockCycleListener(plugin), plugin);
    }

    /** Shared named-inventory snapshots. */
    public static InventoryStore inventories() {
        return inventories;
    }
}
