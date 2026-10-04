package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /tpohere — bring a player to you, bypassing all safety checks.
 * No safe-location search, no tptoggle respect, direct raw teleport.
 */
public final class TpohereCommand extends TeleportCommand {
    public TpohereCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpohere";
    }

    @Override
    public String description() {
        return "Teleport a player to you, bypassing safety checks.";
    }

    @Override
    public String usage() {
        return "/tpohere <player>";
    }

    @Override
    public String permission() {
        return "forgecore.tpohere";
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
            throw fail("You cannot teleport yourself to yourself.");
        }
        // Raw teleport: no safe-location adjustment, no toggle checks.
        target.teleport(self.getLocation());
        plugin.afk().setActive(target);
        Text.send(self, "<gray>Teleported <white>" + Text.escape(target.getName())
                + "</white> to you (override).</gray>");
        Text.send(target, "<gray>You were teleported to <white>" + Text.escape(self.getName())
                + "</white>.</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
