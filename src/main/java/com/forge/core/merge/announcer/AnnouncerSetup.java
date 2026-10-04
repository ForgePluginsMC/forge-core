package com.forge.core.merge.announcer;

import com.forge.core.ForgeCore;

/** Constructs the announcer manager once; the command reads it from here. */
final class AnnouncerSetup {
    private static AnnouncerManager manager;
    private static boolean initialized;

    private AnnouncerSetup() {
    }

    static void init(ForgeCore plugin) {
        if (initialized) {
            return;
        }
        initialized = true;
        manager = new AnnouncerManager(plugin);
        manager.start();
    }

    static AnnouncerManager manager() {
        if (manager == null) {
            throw new IllegalStateException("AnnouncerSetup.init was not called");
        }
        return manager;
    }
}
