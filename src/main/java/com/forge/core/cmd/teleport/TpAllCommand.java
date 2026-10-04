package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpall <player> — teleport every online player to one player. */
public final class TpAllCommand extends TeleportCommand {
    public TpAllCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpall";
    }

    @Override
    public String description() {
        return "Teleport every online player to one player.";
    }

    @Override
    public String usage() {
        return "/tpall <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        int moved = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.equals(target)) {
                continue;
            }
            teleport(player, target.getLocation(),
                    "<gray>You were teleported to <white>" + Text.escape(target.getName()) + "</white>.");
            moved++;
        }
        Text.ok(sender, "Teleported <white>" + moved + "</white> player" + (moved == 1 ? "" : "s")
                + " to <white>" + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
