package com.forge.core.guild;

import com.forge.core.ForgeCore;

/**
 * One-time setup for the guild system: registers the territory/war listener.
 * Called at the top of {@link GuildPack#commands}.
 */
public final class GuildSetup {
    private static boolean done;

    private GuildSetup() {
    }

    public static void init(ForgeCore plugin) {
        if (done) {
            return;
        }
        done = true;
        plugin.getServer().getPluginManager().registerEvents(
                new GuildListener(plugin, plugin.guilds()), plugin);
    }
}
