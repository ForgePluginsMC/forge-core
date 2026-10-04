package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle shift-right-click sign editing. */
public final class ToggleshifteditCommand extends PlayerACommand {
    public ToggleshifteditCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "toggleshiftedit";
    }

    @Override
    public String description() {
        return "Toggle shift-right-click sign editing.";
    }

    @Override
    public String usage() {
        return "/toggleshiftedit";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UserData data = plugin.users().get(player);
        boolean on = !data.getBoolean("shift-edit", false);
        data.setBoolean("shift-edit", on);
        plugin.users().save(player.getUniqueId());
        Text.ok(sender, "Shift-click sign editing " + (on ? "<green>on</green>." : "<red>off</red>."));
    }
}
