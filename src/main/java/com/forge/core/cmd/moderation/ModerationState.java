package com.forge.core.cmd.moderation;

/** Holds pack-owned managers created by {@link ModerationSetup}. */
final class ModerationState {
    private static IpBanManager ipBans;

    private ModerationState() {
    }

    static void ipBans(IpBanManager manager) {
        ipBans = manager;
    }

    static IpBanManager ipBans() {
        return ipBans;
    }
}
