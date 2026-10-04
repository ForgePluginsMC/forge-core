package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** List your powertool bindings. */
public final class PowertoollistCommand extends PlayerACommand {
    public PowertoollistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "powertoollist";
    }

    @Override
    public String description() {
        return "List your powertool bindings.";
    }

    @Override
    public String usage() {
        return "/powertoollist";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Map<Material, String> bindings = PlayerAState.powertools().bindings(player);
        if (bindings.isEmpty()) {
            Text.send(sender, "You have no powertools bound. Use <white>/powertool <command></white> while holding an item.");
            return;
        }
        Text.send(sender, "<white>Your powertools:</white>");
        for (Map.Entry<Material, String> entry : bindings.entrySet()) {
            Text.send(sender, "<gray>" + entry.getKey().name().toLowerCase(Locale.ROOT) + ":</gray> <white>/"
                    + Text.escape(entry.getValue()) + "</white>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
