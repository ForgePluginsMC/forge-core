package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.dsign.DynamicSignManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Dynamic signs: signs whose lines support MiniMessage and placeholders,
 * refreshed on a timer.
 *
 * <p>Usage: /dsign &lt;create|delete|list&gt; [name]
 */
public final class DsignCommand extends ForgeCommand {
    public DsignCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dsign";
    }

    @Override
    public String description() {
        return "Turn the targeted sign into a self-updating dynamic sign.";
    }

    @Override
    public String usage() {
        return "/dsign <create|delete|list> [name]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Missing arguments.");
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        DynamicSignManager manager = DynamicSignManager.get();
        switch (sub) {
            case "list" -> {
                List<String> names = manager.names();
                if (names.isEmpty()) {
                    Text.send(sender, "No dynamic signs defined.");
                } else {
                    Text.send(sender, "Dynamic signs: <white>" + Text.escape(String.join(", ", names)) + "</white>");
                }
            }
            case "create" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/dsign create <name>");
                }
                String name = args[1];
                Block target = targetSign(player);
                if (target == null) {
                    throw new CommandRegistry.CommandFailure("Look at a sign within 5 blocks.");
                }
                if (!manager.create(name, target)) {
                    throw new CommandRegistry.CommandFailure(
                            "A dynamic sign named '" + Text.escape(name) + "' already exists.");
                }
                Text.ok(sender, "Dynamic sign <white>" + Text.escape(name)
                        + "</white> created. Its lines now support placeholders.");
            }
            case "delete" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/dsign delete <name>");
                }
                if (!manager.delete(args[1])) {
                    throw new CommandRegistry.CommandFailure(
                            "No dynamic sign named '" + Text.escape(args[1]) + "'.");
                }
                Text.ok(sender, "Dynamic sign <white>" + Text.escape(args[1]) + "</white> deleted.");
            }
            default -> throw new CommandRegistry.CommandFailure("Unknown subcommand. Usage: " + usage());
        }
    }

    private static @Nullable Block targetSign(Player player) {
        Block block = player.getTargetBlockExact(5);
        if (block != null && block.getState() instanceof Sign) {
            return block;
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("create", "delete", "list"), args);
        }
        if (args.length == 2 && "delete".equalsIgnoreCase(args[0])) {
            return Players.filter(DynamicSignManager.get().names(), args);
        }
        return List.of();
    }
}
