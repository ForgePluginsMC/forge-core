package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.attach.AttachManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Attaches a command to the held item. Modifiers inside the command string:
 * {@code !left!}, {@code !right!}, {@code !limiteduse:[n]!}, {@code !cc!}.
 * With no arguments, removes attached commands from the held item.
 *
 * <p>Usage: /attachcommand [command...]
 */
public final class AttachcommandCommand extends ForgeCommand {
    public AttachcommandCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "attachcommand";
    }

    @Override
    public List<String> aliases() {
        return List.of("attachcmd");
    }

    @Override
    public String description() {
        return "Attach a command to the held item (or clear it).";
    }

    @Override
    public String usage() {
        return "/attachcommand [command...]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        AttachManager manager = AttachManager.get();
        if (args.length == 0) {
            if (!manager.clear(player)) {
                throw new CommandRegistry.CommandFailure(
                        "Your held item has no attached command (or your hand is empty).");
            }
            Text.ok(sender, "Attached commands removed from your held item.");
            return;
        }
        String command = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        if (!manager.attach(player, command)) {
            throw new CommandRegistry.CommandFailure("Hold an item in your main hand first.");
        }
        List<String> attached = manager.attached(player);
        int total = attached == null ? 1 : attached.size();
        Text.ok(sender, "Command attached. Modifiers: <white>!left!</white> <white>!right!</white> "
                + "<white>!limiteduse:[n]!</white> <white>!cc!</white> "
                + "<gray>(<white>" + total + "</white> attached)</gray>");
        Text.send(sender, "<gray>Attached:</gray> <white>" + Text.escape(command) + "</white>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
