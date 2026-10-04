package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.KitManager;
import com.forge.core.data.KitManager.Kit;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** List kits, or claim one. */
public final class KitCommand extends ForgeCommand {
    public KitCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "kit";
    }

    @Override
    public String description() {
        return "List kits, or claim a kit.";
    }

    @Override
    public String usage() {
        return "/kit [name]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            List<String> names = plugin.kits().names();
            if (names.isEmpty()) {
                Text.send(player, "<gray>No kits yet.");
                return;
            }
            StringBuilder list = new StringBuilder("<gold>Kits: <white>");
            for (int i = 0; i < names.size(); i++) {
                if (i > 0) {
                    list.append("<gray>, <white>");
                }
                list.append(Text.escape(names.get(i)));
            }
            Text.send(player, list.toString());
            return;
        }
        @Nullable Kit kit = plugin.kits().get(args[0]);
        if (kit == null) {
            throw new CommandRegistry.CommandFailure(
                    "No kit named <white>" + Text.escape(args[0]) + "</white>.");
        }
        long remaining = plugin.kits().cooldownRemaining(player, kit);
        if (remaining > 0) {
            throw new CommandRegistry.CommandFailure("Wait <white>" + Time.format(remaining)
                    + "</white> before claiming that kit again.");
        }
        if (kit.cost() > 0 && !plugin.economy().take(player.getUniqueId(), kit.cost())) {
            throw new CommandRegistry.CommandFailure("That kit costs "
                    + plugin.economy().format(kit.cost()) + ".");
        }
        plugin.kits().give(player, kit);
        Text.ok(player, "Claimed kit <white>" + Text.escape(kit.name()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(plugin.kits().names(), args);
        }
        return List.of();
    }
}
