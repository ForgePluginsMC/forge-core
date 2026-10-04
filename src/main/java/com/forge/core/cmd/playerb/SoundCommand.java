package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /sound — play a sound (namespaced key) at a player. */
public final class SoundCommand extends ForgeCommand {
    private static final List<String> SOUND_KEYS = Registry.SOUNDS.stream()
            .map(sound -> {
                NamespacedKey key = Registry.SOUNDS.getKey(sound);
                return key == null ? null : key.asString();
            })
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());

    public SoundCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sound";
    }

    @Override
    public String description() {
        return "Play a sound at a player (namespaced key, e.g. minecraft:entity.player.levelup).";
    }

    @Override
    public String usage() {
        return "/sound <sound> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            Text.usage(sender, usage());
            return;
        }
        Key key;
        try {
            key = Key.key(args[0]);
        } catch (IllegalArgumentException bad) {
            Text.error(sender, "<white>" + Text.escape(args[0]) + "</white> is not a valid sound key.");
            return;
        }
        Player target;
        if (args.length == 2) {
            if (!sender.hasPermission("forgecore.sound.others")) {
                Text.error(sender, "You don't have permission to do that.");
                return;
            }
            target = Players.find(sender, args[1]);
            if (target == null) {
                return;
            }
        } else {
            target = asPlayer(sender);
            if (target == null) {
                Text.error(sender, "Only players can use that command.");
                return;
            }
        }
        target.playSound(Sound.sound(key, Sound.Source.MASTER, 1.0f, 1.0f));
        Text.ok(sender, "Played <white>" + Text.escape(key.asString()) + "</white>"
                + (target.equals(asPlayer(sender)) ? "." : " at <white>" + Text.escape(target.getName()) + "</white>."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(SOUND_KEYS, args);
        }
        if (args.length == 2 && sender.hasPermission("forgecore.sound.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
