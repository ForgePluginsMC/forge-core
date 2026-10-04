package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Toggle the global chat mute. */
public final class MutechatCommand extends ForgeCommand {
    public MutechatCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mutechat";
    }

    @Override
    public String description() {
        return "Toggle muting chat server-wide.";
    }

    @Override
    public String usage() {
        return "/mutechat";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        boolean now = !plugin.mutes().chatMuted();
        plugin.mutes().setChatMuted(now);
        if (now) {
            Text.broadcast("<red>Chat has been muted by <white>" + Text.escape(sender.getName()) + "</white>.");
        } else {
            Text.broadcast("<green>Chat has been unmuted by <white>" + Text.escape(sender.getName()) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
