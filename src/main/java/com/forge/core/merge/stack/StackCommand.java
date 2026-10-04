package com.forge.core.merge.stack;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * {@code /stack} — stacking controls: reload, stackall, clearall,
 * givespawner, info, toggle.
 */
public final class StackCommand extends ForgeCommand {

    private static final List<String> SUBS = List.of(
            "reload", "stackall", "clearall", "givespawner", "info", "toggle");

    public StackCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "stack";
    }

    @Override
    public List<String> aliases() {
        return List.of("fstack");
    }

    @Override
    public String description() {
        return "Entity, item and spawner stacking controls.";
    }

    @Override
    public String usage() {
        return "/stack <reload|stackall|clearall|givespawner|info|toggle>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Missing subcommand.", usage());
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                StackSetup.reload();
                Text.ok(sender, "Stacking configuration reloaded.");
            }
            case "stackall" -> {
                int merges = StackSetup.stacks().scan();
                Text.ok(sender, "Stack scan complete: <white>" + merges + "</white> merge(s).");
            }
            case "clearall" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/" + label + " clearall <entities|items>");
                }
                switch (args[1].toLowerCase(Locale.ROOT)) {
                    case "entities" -> {
                        int removed = StackSetup.stacks().clearStackedEntities();
                        Text.ok(sender, "Removed <white>" + removed + "</white> stacked entities.");
                    }
                    case "items" -> {
                        int removed = StackSetup.stacks().clearItems();
                        Text.ok(sender, "Removed <white>" + removed + "</white> dropped items.");
                    }
                    default -> throw new CommandRegistry.CommandFailure("Unknown target.", "/" + label + " clearall <entities|items>");
                }
            }
            case "givespawner" -> giveSpawner(sender, label, args);
            case "info" -> {
                int[] info = StackSetup.stacks().info();
                Text.send(sender, "<gold>Stacking info:");
                Text.send(sender, " <gray>Stacked entities: <white>" + info[0] + "</white>");
                Text.send(sender, " <gray>Total mobs in stacks: <white>" + info[1] + "</white>");
                Text.send(sender, " <gray>Stacked spawners: <white>" + info[2] + "</white>");
                Text.send(sender, " <gray>Status: " + (StackSetup.settings().enabled() ? "<green>enabled" : "<red>disabled") + "</gray>");
            }
            case "toggle" -> {
                boolean next = StackSetup.toggle();
                Text.ok(sender, "Stacking " + (next ? "<green>enabled</green>." : "<red>disabled</red>."));
            }
            default -> throw new CommandRegistry.CommandFailure("Unknown subcommand.", usage());
        }
    }

    private void giveSpawner(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            throw new CommandRegistry.CommandFailure("Missing arguments.", "/" + label + " givespawner <type> <count> [player]");
        }
        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CommandRegistry.CommandFailure("Unknown entity type: <white>" + Text.escape(args[1]) + "</white>.");
        }
        int count;
        try {
            count = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new CommandRegistry.CommandFailure("Count must be a number.");
        }
        count = Math.max(1, Math.min(1000, count));
        Player target = asPlayer(sender);
        if (args.length >= 4) {
            Player found = Players.find(sender, args[3]);
            if (found == null) {
                return;
            }
            target = found;
        }
        if (target == null) {
            throw new CommandRegistry.CommandFailure("Only players can receive spawners from the console without a target.");
        }
        ItemStack item = StackSetup.spawners().createSpawnerItem(type, count);
        target.getInventory().addItem(item);
        Text.ok(sender, "Gave <white>" + count + "x " + StackManager.prettyName(type) + "</white> spawner to <white>"
                + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(new ArrayList<>(SUBS), args);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("clearall")) {
            return Players.filter(List.of("entities", "items"), args);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("givespawner")) {
            String prefix = args[1].toUpperCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type.name().startsWith(prefix)) {
                    out.add(type.name().toLowerCase(Locale.ROOT));
                }
            }
            return out;
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("givespawner")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
