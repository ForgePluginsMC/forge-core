package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * /dashboard — open the admin dashboard GUI.
 *
 * <p>Admin-only. Shows server health, world management, feature toggles,
 * diagnostics, update alerts, and a setup assistant.
 */
@NullMarked
public final class DashboardCommand extends ForgeCommand {
    public DashboardCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dashboard";
    }

    @Override
    public List<String> aliases() {
        return List.of("dash", "adminpanel");
    }

    @Override
    public String description() {
        return "Open the admin dashboard.";
    }

    @Override
    public String usage() {
        return "/dashboard";
    }

    @Override
    public String permission() {
        return "forgecore.dashboard";
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
        if (!player.hasPermission("forgecore.dashboard") && !player.isOp()) {
            Text.error(player, "You don't have permission to use the dashboard.");
            return;
        }
        new DashboardGui(plugin).open(player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
