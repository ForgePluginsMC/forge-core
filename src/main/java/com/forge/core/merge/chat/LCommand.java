package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /l — send a one-shot message to local (nearby) chat. */
public final class LCommand extends ForgeCommand {
    public LCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "l";
    }

    @Override
    public String description() {
        return "Send a one-shot message to nearby players.";
    }

    @Override
    public String usage() {
        return "/l <message...>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        ChatManager manager = ChatManager.get();
        if (manager == null) {
            Text.error(sender, "Chat is not ready yet.");
            return;
        }
        GCommand.sendOneShot(manager, player, ChatChannel.LOCAL, String.join(" ", args));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
