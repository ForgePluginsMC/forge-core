package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle receiving payments: /paytoggle blocks incoming /money pay. */
public final class PaytoggleCommand extends ForgeCommand {
    public PaytoggleCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "paytoggle";
    }

    @Override
    public String description() {
        return "Toggle receiving money payments.";
    }

    @Override
    public String usage() {
        return "/paytoggle";
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
        boolean now = !data.getBoolean("paytoggle", false);
        data.setBoolean("paytoggle", now);
        Text.ok(sender, "Payment receiving " + (now ? "disabled" : "enabled") + ".");
    }

    /** True when the player blocks incoming payments. */
    public static boolean blocked(ForgeCore plugin, Player player) {
        return plugin.users().get(player).getBoolean("paytoggle", false);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
