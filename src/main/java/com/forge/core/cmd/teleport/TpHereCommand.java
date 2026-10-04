package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tphere <player> — teleport a player to you. */
public final class TpHereCommand extends TeleportCommand {
    public TpHereCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tphere";
    }

    @Override
    public List<String> aliases() {
        return List.of("s");
    }

    @Override
    public String description() {
        return "Teleport a player to your location.";
    }

    @Override
    public String usage() {
        return "/tphere <player>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player self = requirePlayer(sender);
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        if (target.equals(self)) {
            throw fail("You are already here.");
        }
        teleport(target, self.getLocation(),
                "<gray>Teleported to <white>" + Text.escape(self.getName()) + "</white>.");
        Text.send(sender, "<gray>Teleported <white>" + Text.escape(target.getName()) + "</white> to you.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
