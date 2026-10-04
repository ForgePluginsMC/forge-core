package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle receiving private messages. */
public final class MsgtoggleCommand extends ForgeCommand {
    public MsgtoggleCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "msgtoggle";
    }

    @Override
    public String description() {
        return "Toggle receiving private messages.";
    }

    @Override
    public String usage() {
        return "/msgtoggle";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        var data = plugin.users().get(player);
        boolean now = !data.getBoolean("msgtoggle", false);
        data.setBoolean("msgtoggle", now);
        Text.ok(sender, "Private messages " + (now ? "disabled" : "enabled") + ".");
    }

    /** True when the player blocks incoming private messages. */
    public static boolean blocked(ForgeCore plugin, Player player) {
        return plugin.users().get(player).getBoolean("msgtoggle", false);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
