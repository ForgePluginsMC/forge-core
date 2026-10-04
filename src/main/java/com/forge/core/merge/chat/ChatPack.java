package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * forge-chat commands merged into ForgeCore: channels (/ch, /g, /l),
 * per-channel slowmode and chat config reload.
 */
public final class ChatPack {
    private ChatPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        ChatSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new ChCommand(plugin));
        commands.add(new GCommand(plugin));
        commands.add(new LCommand(plugin));
        commands.add(new SlowmodeCommand(plugin));
        commands.add(new ChatreloadCommand(plugin));
        return commands;
    }
}
