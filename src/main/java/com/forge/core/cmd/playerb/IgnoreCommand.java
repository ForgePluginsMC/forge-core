package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /ignore — toggle ignoring a player's chat and private messages. */
public final class IgnoreCommand extends ForgeCommand {
    public IgnoreCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ignore";
    }

    @Override
    public String description() {
        return "Toggle ignoring a player.";
    }

    @Override
    public String usage() {
        return "/ignore <player>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        Player online = Players.findQuiet(args[0]);
        UUID targetUuid;
        String targetName;
        if (online != null) {
            targetUuid = online.getUniqueId();
            targetName = online.getName();
        } else {
            OfflinePlayer offline = Players.offline(args[0]);
            if (offline == null) {
                Text.error(sender, "Player <white>" + Text.escape(args[0]) + "</white> has never played here.");
                return;
            }
            targetUuid = offline.getUniqueId();
            targetName = offline.getName() == null ? args[0] : offline.getName();
        }
        if (targetUuid.equals(player.getUniqueId())) {
            Text.error(sender, "You cannot ignore yourself.");
            return;
        }
        boolean nowIgnoring = MsgManager.get().toggleIgnore(player.getUniqueId(), targetUuid);
        if (nowIgnoring) {
            Text.ok(sender, "You are now ignoring <white>" + Text.escape(targetName) + "</white>.");
        } else {
            Text.ok(sender, "You are no longer ignoring <white>" + Text.escape(targetName) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
