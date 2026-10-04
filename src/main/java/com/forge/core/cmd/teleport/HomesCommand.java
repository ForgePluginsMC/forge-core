package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /homes [player] — list homes; other players' homes with forgecore.homes.others. */
public final class HomesCommand extends TeleportCommand {
    public HomesCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "homes";
    }

    @Override
    public String description() {
        return "List homes (yours, or another player's with permission).";
    }

    @Override
    public String usage() {
        return "/homes [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        String ownerName;
        UUID ownerUuid;
        if (args.length == 0) {
            Player player = requirePlayer(sender);
            ownerName = player.getName();
            ownerUuid = player.getUniqueId();
        } else {
            if (!sender.hasPermission("forgecore.homes.others")) {
                throw fail("You don't have permission to view other players' homes.");
            }
            OfflinePlayer offline = Players.offline(args[0]);
            if (offline == null) {
                throw fail("Player <white>" + Text.escape(args[0]) + "</white> has never played here.");
            }
            ownerName = offline.getName() == null ? args[0] : offline.getName();
            ownerUuid = offline.getUniqueId();
        }
        Map<String, Location> homes = plugin.users().get(ownerUuid).homes();
        if (homes.isEmpty()) {
            Text.send(sender, "<gray>" + Text.escape(ownerName) + " has no homes set.");
            return;
        }
        List<String> names = new ArrayList<>(homes.keySet());
        Text.send(sender, "<gray>Homes of <white>" + Text.escape(ownerName) + "</white> (" + names.size() + "): <white>"
                + Text.escape(String.join("<gray>, <white>", names)));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.homes.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
