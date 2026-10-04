package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/** Fully heal: health, food, saturation, fire and potion effects. */
public final class HealCommand extends PlayerACommand {
    public HealCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "heal";
    }

    @Override
    public String description() {
        return "Fully heal yourself or another player.";
    }

    @Override
    public String usage() {
        return "/heal [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        var attribute = target.getAttribute(Attribute.MAX_HEALTH);
        target.setHealth(attribute == null ? 20.0 : attribute.getBaseValue());
        target.setFoodLevel(20);
        target.setSaturation(20.0f);
        target.setFireTicks(0);
        for (PotionEffect active : new ArrayList<>(target.getActivePotionEffects())) {
            target.removePotionEffect(active.getType());
        }
        Text.ok(sender, "Healed <white>" + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "You were healed.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
