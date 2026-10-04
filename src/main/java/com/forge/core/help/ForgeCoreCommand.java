package com.forge.core.help;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * {@code /forgecore} — plugin root command. Currently hosts the help
 * subcommand (usable from console too); players usually reach the same pages
 * via {@code /help forgecore}.
 */
public final class ForgeCoreCommand extends ForgeCommand {
    public ForgeCoreCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "forgecore";
    }

    @Override
    public List<String> aliases() {
        return List.of("fc");
    }

    @Override
    public String description() {
        return "ForgeCore plugin info and command help.";
    }

    @Override
    public String permission() {
        return "";
    }

    @Override
    public String usage() {
        return "/forgecore help [page|command]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            plugin.help().showHelp(sender, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        Text.send(sender, "<gold><bold>ForgeCore</bold></gold> <gray>v"
                + plugin.getPluginMeta().getVersion() + " — <white>" + CommandRegistry.count()
                + "</white> commands. See <yellow>/forgecore help</yellow> or <yellow>/help forgecore</yellow>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("help");
        }
        return List.of();
    }
}
