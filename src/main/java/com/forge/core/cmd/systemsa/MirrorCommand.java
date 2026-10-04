package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.mirror.MirrorManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Mirrors your block placements across 11 symmetry modes.
 *
 * <p>Usage: /mirror &lt;start|stop|mode&gt; [mode]
 */
public final class MirrorCommand extends ForgeCommand {
    public MirrorCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mirror";
    }

    @Override
    public String description() {
        return "Mirror your block placements across a symmetry mode.";
    }

    @Override
    public String usage() {
        return "/mirror <start|stop|mode> [mode]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Usage: " + usage());
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        MirrorManager manager = MirrorManager.get();
        switch (sub) {
            case "mode" -> {
                if (args.length == 1) {
                    Text.send(sender, "<gold>Mirror modes:</gold>");
                    for (String mode : MirrorManager.MODES) {
                        String description = MirrorManager.DESCRIPTIONS.get(mode);
                        Text.send(sender, "  <white>" + Text.escape(mode) + "</white> <gray>— "
                                + Text.escape(description == null ? "" : description) + "</gray>");
                    }
                    String current = manager.modeOf(player);
                    Text.send(sender, "Current: <white>"
                            + Text.escape(current == null ? "off" : current) + "</white>");
                } else {
                    setMode(sender, player, manager, args[1]);
                }
            }
            case "start" -> setMode(sender, player, manager,
                    args.length > 1 ? args[1] : modeOrDefault(manager, player));
            case "stop" -> {
                manager.stop(player);
                Text.send(sender, "Mirror <white>off</white>.");
            }
            default -> throw new CommandRegistry.CommandFailure("Unknown subcommand. Usage: " + usage());
        }
    }

    private static String modeOrDefault(MirrorManager manager, Player player) {
        String current = manager.modeOf(player);
        return current == null ? "x" : current;
    }

    private static void setMode(CommandSender sender, Player player, MirrorManager manager, String mode) {
        String normalized = mode.toLowerCase(Locale.ROOT);
        if (!manager.isValidMode(normalized)) {
            throw new CommandRegistry.CommandFailure("Unknown mode '" + Text.escape(mode)
                    + "'. Use <white>/mirror mode</white> to list them.");
        }
        manager.start(player, normalized);
        Text.ok(sender, "Mirror <white>on</white> — mode <white>" + Text.escape(normalized)
                + "</white>. Your placements are mirrored around you.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("start", "stop", "mode"), args);
        }
        if (args.length == 2 && ("start".equalsIgnoreCase(args[0]) || "mode".equalsIgnoreCase(args[0]))) {
            return Players.filter(new ArrayList<>(MirrorManager.MODES), args);
        }
        return List.of();
    }
}
