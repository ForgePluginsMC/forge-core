package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle totem auto-use from anywhere in the inventory. */
public final class ToggletotemCommand extends PlayerACommand {
    public ToggletotemCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "toggletotem";
    }

    @Override
    public String description() {
        return "Toggle totem auto-use from your inventory.";
    }

    @Override
    public String usage() {
        return "/toggletotem";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UserData data = plugin.users().get(player);
        boolean on = !data.getBoolean("totem", false);
        data.setBoolean("totem", on);
        plugin.users().save(player.getUniqueId());
        Text.ok(sender, "Totem auto-use " + (on ? "<green>on</green>." : "<red>off</red>."));
    }
}
