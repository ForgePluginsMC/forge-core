package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Apply a potion effect, or clear all of a player's effects. */
public final class EffectCommand extends PlayerACommand {
    public EffectCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "effect";
    }

    @Override
    public String description() {
        return "Apply or clear potion effects.";
    }

    @Override
    public String usage() {
        return "/effect <player> <effect|clear> [duration-sec] [amplifier]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = requiredTarget(sender, args);
        if (target == null) {
            return;
        }
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        if (args[1].equalsIgnoreCase("clear")) {
            for (PotionEffect active : new ArrayList<>(target.getActivePotionEffects())) {
                target.removePotionEffect(active.getType());
            }
            Text.ok(sender, "Cleared all effects from <white>" + Text.escape(target.getName()) + "</white>.");
            return;
        }
        PotionEffectType type = Effects.find(args[1]);
        if (type == null) {
            Text.error(sender, "Unknown effect <white>" + Text.escape(args[1]) + "</white>.");
            return;
        }
        int seconds = 30;
        int amplifier = 0;
        if (args.length >= 3) {
            try {
                seconds = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception) {
                Text.error(sender, "Duration must be seconds as a number.");
                return;
            }
        }
        if (args.length >= 4) {
            try {
                amplifier = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                Text.error(sender, "Amplifier must be a number.");
                return;
            }
        }
        if (seconds <= 0 || amplifier < 0) {
            Text.error(sender, "Duration must be positive and amplifier non-negative.");
            return;
        }
        target.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier));
        Text.ok(sender, "Applied <white>" + Text.escape(type.getKey().getKey()) + " " + (amplifier + 1)
                + "</white> to <white>" + Text.escape(target.getName()) + "</white> for <white>"
                + seconds + "s</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return playerNames(args);
        }
        if (args.length == 2) {
            List<String> options = new ArrayList<>(Effects.keys());
            options.add("clear");
            return Players.filter(options, args);
        }
        return List.of();
    }
}
