package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Create, delete and list jails. */
public final class JaileditCommand extends ForgeCommand {
    public JaileditCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "jailedit";
    }

    @Override
    public String description() {
        return "Create, delete or list jails.";
    }

    @Override
    public String usage() {
        return "/jailedit <create|delete|list> [name]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                List<String> names = plugin.jails().names();
                if (names.isEmpty()) {
                    Text.send(sender, "No jails defined.");
                } else {
                    Text.send(sender, "<gold>Jails:</gold> <white>" + Text.escape(String.join("<gray>, </gray><white>", names)));
                }
            }
            case "create" -> {
                if (!(sender instanceof Player player)) {
                    Text.error(sender, "Only players can use that command.");
                    return;
                }
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/jailedit create <name>");
                }
                plugin.jails().set(args[1], player.getLocation());
                Text.ok(sender, "Jail <white>" + Text.escape(args[1]) + "</white> created at your location.");
            }
            case "delete" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/jailedit delete <name>");
                }
                if (plugin.jails().remove(args[1])) {
                    Text.ok(sender, "Jail <white>" + Text.escape(args[1]) + "</white> deleted.");
                } else {
                    throw new CommandRegistry.CommandFailure("Unknown jail: <white>" + Text.escape(args[1]) + "</white>.");
                }
            }
            default -> Text.usage(sender, usage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("create", "delete", "list"), args);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            return Players.filter(plugin.jails().names(), args);
        }
        return List.of();
    }
}
