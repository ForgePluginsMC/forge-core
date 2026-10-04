package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.alias.AliasManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;

/**
 * In-game custom alias editor: {@code /alias args...} rewrites to the
 * mapped command plus args.
 *
 * <p>Usage: /aliaseditor &lt;create|delete|list&gt; &lt;alias&gt; [command...]
 */
public final class AliaseditorCommand extends ForgeCommand {
    public AliaseditorCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "aliaseditor";
    }

    @Override
    public String description() {
        return "Create and manage custom command aliases.";
    }

    @Override
    public String usage() {
        return "/aliaseditor <create|delete|list> <alias> [command...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Missing arguments.");
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        AliasManager manager = AliasManager.get();
        switch (sub) {
            case "list" -> {
                List<String> names = manager.names();
                if (names.isEmpty()) {
                    Text.send(sender, "No aliases defined.");
                } else {
                    Text.send(sender, "<gold>Aliases:</gold>");
                    for (String name : names) {
                        Text.send(sender, "  <white>/" + Text.escape(name) + "</white> <gray>→ /"
                                + Text.escape(manager.get(name)) + "</gray>");
                    }
                }
            }
            case "create" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/aliaseditor create <alias> <command...>");
                }
                if (args.length < 3) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/aliaseditor create <alias> <command...>");
                }
                String alias = args[1];
                String command = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                if (!manager.create(alias, command)) {
                    throw new CommandRegistry.CommandFailure(
                            "Could not create that alias (duplicate or empty).");
                }
                Text.ok(sender, "Alias <white>/" + Text.escape(AliasManager.normalize(alias))
                        + "</white> → <white>/" + Text.escape(command) + "</white> created.");
            }
            case "delete" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/aliaseditor delete <alias>");
                }
                if (!manager.delete(args[1])) {
                    throw new CommandRegistry.CommandFailure(
                            "No alias named '" + Text.escape(args[1]) + "'.");
                }
                Text.ok(sender, "Alias <white>/" + Text.escape(AliasManager.normalize(args[1]))
                        + "</white> deleted.");
            }
            default -> throw new CommandRegistry.CommandFailure("Unknown subcommand. Usage: " + usage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("create", "delete", "list"), args);
        }
        if (args.length == 2 && "delete".equalsIgnoreCase(args[0])) {
            return Players.filter(AliasManager.get().names(), args);
        }
        return List.of();
    }
}
