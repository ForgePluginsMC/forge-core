package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/** Clear potion effects, fire and freeze. */
public final class ShakeitoffCommand extends PlayerACommand {
    public ShakeitoffCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "shakeitoff";
    }

    @Override
    public String description() {
        return "Clear effects, fire and freeze.";
    }

    @Override
    public String usage() {
        return "/shakeitoff";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        for (PotionEffect active : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(active.getType());
        }
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        Text.ok(sender, "Shook it off.");
    }
}
