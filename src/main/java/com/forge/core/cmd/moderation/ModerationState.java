package com.forge.core.cmd.moderation;

/** Holds pack-owned managers created by {@link ModerationSetup}. */
final class ModerationState {
    private static IpBanManager ipBans;
    private static WarnManager warns;
    private static SignSpyListener signSpy;

    private ModerationState() {
    }

    static void ipBans(IpBanManager manager) {
        ipBans = manager;
    }

    static IpBanManager ipBans() {
        return ipBans;
    }

    static void warns(WarnManager manager) {
        warns = manager;
    }

    static WarnManager warns() {
        return warns;
    }

    static void signSpy(SignSpyListener listener) {
        signSpy = listener;
    }

    static SignSpyListener signSpy() {
        return signSpy;
    }
}
