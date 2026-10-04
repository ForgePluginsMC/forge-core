package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /tpo — teleport to a player, bypassing all safety checks.
 * No safe-location search, no tptoggle respect, direct raw teleport.
 */
public final class TpoCommand extends TeleportCommand {
    public TpoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpo";
    }

    @Override
    public String description() {
        return "Teleport to a player, bypassing safety checks.";
    }

    @Override
    public String usage() {
        return "/tpo <player>";
    }

    @Override
    public String permission() {
        return "forgecore.tpo";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player self = requirePlayer(sender);
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        if (self.equals(target)) {
            throw fail("You cannot teleport to yourself.");
        }
        // Raw teleport: no safe-location adjustment, no toggle checks.
        self.teleport(target.getLocation());
        plugin.afk().setActive(self);
        Text.send(self, "<gray>Teleported to <white>" + Text.escape(target.getName())
                + "</white> (override).</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
