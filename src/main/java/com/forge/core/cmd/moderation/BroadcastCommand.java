package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Broadcast a MiniMessage-formatted message to the whole server. */
public final class BroadcastCommand extends ForgeCommand {
    public BroadcastCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "broadcast";
    }

    @Override
    public List<String> aliases() {
        return List.of("bc");
    }

    @Override
    public String description() {
        return "Broadcast a message to the whole server.";
    }

    @Override
    public String usage() {
        return "/broadcast <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        // Staff-gated command, so sender MiniMessage markup is trusted here.
        Text.broadcast(String.join(" ", args));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
