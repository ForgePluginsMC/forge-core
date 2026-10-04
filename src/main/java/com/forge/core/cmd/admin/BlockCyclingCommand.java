package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /blockcycling — toggle right-click block variant cycling. */
public final class BlockCyclingCommand extends ForgeCommand {
    public BlockCyclingCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "blockcycling";
    }

    @Override
    public String description() {
        return "Toggle right-click block variant cycling.";
    }

    @Override
    public String usage() {
        return "/blockcycling";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UserData data = plugin.users().get(player);
        boolean on = !data.getBoolean("blockcycling", false);
        data.setBoolean("blockcycling", on);
        plugin.users().save(player.getUniqueId());
        Text.ok(sender, "Block cycling " + (on ? "<green>enabled</green>" : "<red>disabled</red>")
                + "<green>. Right-click a block to cycle its variant.</green>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
