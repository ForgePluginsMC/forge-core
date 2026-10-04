package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Bind a command to the held item type: /powertool <command...> (empty clears). */
public final class PowertoolCommand extends PlayerACommand {
    public PowertoolCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "powertool";
    }

    @Override
    public String description() {
        return "Bind a command to the item in your hand.";
    }

    @Override
    public String usage() {
        return "/powertool <command...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            Text.error(sender, "Hold an item first.");
            return;
        }
        PowertoolManager manager = PlayerAState.powertools();
        if (args.length == 0) {
            if (manager.unbind(player, held.getType())) {
                Text.ok(sender, "Powertool cleared for <white>" + held.getType().name().toLowerCase(java.util.Locale.ROOT) + "</white>.");
            } else {
                Text.error(sender, "No powertool bound to that item. Usage: " + usage());
            }
            return;
        }
        String command = String.join(" ", args);
        manager.bind(player, held.getType(), command);
        Text.ok(sender, "Bound <white>/" + Text.escape(command) + "</white> to <white>"
                + held.getType().name().toLowerCase(java.util.Locale.ROOT) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
