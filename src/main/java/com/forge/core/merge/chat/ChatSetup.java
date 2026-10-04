package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;

/** Boots the merged chat systems. Idempotent. */
public final class ChatSetup {
    private static boolean done;

    private ChatSetup() {
    }

    public static void init(ForgeCore plugin) {
        if (done) {
            return;
        }
        done = true;
        new ChatManager(plugin);
    }
}
