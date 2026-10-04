package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;

/**
 * Base for portable workstation commands: opens the GUI anywhere via the
 * Menu Type API, anchored at the player's location with reach checks off.
 */
abstract class StationCommand extends ForgeCommand {
    StationCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public final boolean playerOnly() {
        return true;
    }

    /** The menu type to open. */
    protected abstract MenuType.Typed<?, ?> menuType();

    /** Display title for the GUI. */
    protected abstract String title();

    @Override
    public final void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        var builder = menuType().builder();
        if (builder instanceof org.bukkit.inventory.view.builder.LocationInventoryViewBuilder<?> loc) {
            loc.location(player.getLocation()).checkReachable(false);
        }
        builder.title(Component.text(title())).build(player).open();
        Text.ok(sender, title() + " opened.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
