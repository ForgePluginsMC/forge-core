package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;

/**
 * One-time setup for the economy pack: registers listeners.
 * Called at the top of {@link EconomyPack#commands}.
 */
public final class EconomySetup {
    private static boolean done;

    private EconomySetup() {
    }

    public static void init(ForgeCore plugin) {
        if (done) {
            return;
        }
        done = true;
        plugin.getServer().getPluginManager().registerEvents(new ChequeListener(plugin), plugin);
    }
}
