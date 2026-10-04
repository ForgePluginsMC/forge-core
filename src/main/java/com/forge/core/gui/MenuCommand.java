package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * /menu — open the GUI command hub.
 */
@NullMarked
public final class MenuCommand extends ForgeCommand {
    public MenuCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "menu";
    }

    @Override
    public List<String> aliases() {
        return List.of("menus", "gui");
    }

    @Override
    public String description() {
        return "Open the GUI command menu.";
    }

    @Override
    public String usage() {
        return "/menu";
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
        HubGui.open(plugin, player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
