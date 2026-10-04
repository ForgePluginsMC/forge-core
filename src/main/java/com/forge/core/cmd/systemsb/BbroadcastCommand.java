package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Broadcast a message to every server on the BungeeCord network. */
public final class BbroadcastCommand extends ForgeCommand {
    public BbroadcastCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "bbroadcast";
    }

    @Override
    public String description() {
        return "Broadcast a message across the BungeeCord network.";
    }

    @Override
    public String usage() {
        return "/bbroadcast <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        if (!SystemsBSetup.bungee().broadcast(message)) {
            throw new CommandRegistry.CommandFailure("Nobody is online to carry the message.");
        }
        Text.ok(sender, "Network broadcast sent.");
    }
}
