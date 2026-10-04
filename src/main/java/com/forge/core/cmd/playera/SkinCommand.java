package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Change your skin to another player's skin at runtime via Paper's
 * PlayerProfile API.
 */
public final class SkinCommand extends PlayerACommand {
    public SkinCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "skin";
    }

    @Override
    public String description() {
        return "Change your skin to another player's skin.";
    }

    @Override
    public String usage() {
        return "/skin <player|reset>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("clear")) {
            var own = Bukkit.createProfile(player.getUniqueId(), player.getName());
            own.completeFromCache();
            player.setPlayerProfile(own);
            Text.ok(sender, "Skin reset to your own.");
            return;
        }
        var profile = Bukkit.createProfile(args[0]);
        boolean completed;
        try {
            completed = profile.completeFromCache();
            if (!completed) {
                completed = profile.complete(true);
            }
        } catch (Exception e) {
            Text.error(sender, "Could not fetch skin for <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        if (!completed || !profile.hasTextures()) {
            Text.error(sender, "No skin found for <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        player.setPlayerProfile(profile);
        Text.ok(sender, "Skin changed to <white>" + Text.escape(args[0]) + "</white>'s skin.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return playerNames(args);
        }
        return List.of();
    }
}
