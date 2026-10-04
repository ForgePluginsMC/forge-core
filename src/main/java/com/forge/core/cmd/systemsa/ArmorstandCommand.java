package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.armorstand.ArmorStandEditor;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

/**
 * Opens the armor stand editor GUI for the nearest armor stand within
 * 6 blocks.
 *
 * <p>Usage: /armorstand
 */
public final class ArmorstandCommand extends ForgeCommand {
    public ArmorstandCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "armorstand";
    }

    @Override
    public List<String> aliases() {
        return List.of("asedit");
    }

    @Override
    public String description() {
        return "Open the armor stand editor for the nearest armor stand.";
    }

    @Override
    public String usage() {
        return "/armorstand";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ArmorStand stand = ArmorStandEditor.nearest(player, 6.0);
        if (stand == null) {
            Text.error(sender, "No armor stand within 6 blocks.");
            return;
        }
        ArmorStandEditor.get().open(player, stand);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
