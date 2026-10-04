package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;

/**
 * One-time setup for the moderation pack: constructs the managers and
 * listeners that live beyond a single command execution. Called at the top
 * of {@link ModerationPack#commands(ForgeCore)}.
 */
public final class ModerationSetup {
    private static VanishManager vanish;
    private static MaintenanceManager maintenance;
    private static PatrolManager patrol;

    private ModerationSetup() {
    }

    /** Construct pack-level managers/listeners. Safe to call once. */
    public static void init(ForgeCore plugin) {
        if (vanish != null) {
            return;
        }
        vanish = new VanishManager(plugin);
        maintenance = new MaintenanceManager(plugin);
        patrol = new PatrolManager(plugin);
        ModerationState.ipBans(new IpBanManager(plugin));
        ModerationState.warns(new WarnManager(plugin));
        new InvViewGuard(plugin);
        new SessionTracker(plugin);
        ModerationState.signSpy(new SignSpyListener(plugin));
    }

    public static VanishManager vanish() {
        return vanish;
    }

    public static MaintenanceManager maintenance() {
        return maintenance;
    }

    public static PatrolManager patrol() {
        return patrol;
    }
}
