package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.PermissionManager;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Check whether a player has a permission, showing exactly how it resolved. */
public final class PermCommand extends ForgeCommand {
    public PermCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "perm";
    }

    @Override
    public String description() {
        return "Check a player's permission with resolution details.";
    }

    @Override
    public String usage() {
        return "/perm <player> <node>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, args[0]);
        if (uuid == null) {
            return;
        }
        String node = args[1].toLowerCase(Locale.ROOT);
        PermissionManager.CheckResult result = plugin.permissions().check(uuid, node, contextOf(uuid));
        Text.send(sender, "<white>" + Text.escape(PermUtil.displayName(uuid)) + "</white> <gray>"
                + Text.escape(node) + " -> "
                + (result.allowed() ? "<green>ALLOW" : "<red>DENY")
                + " <gray>(" + Text.escape(result.trace()) + ")</gray>");
    }

    private Map<String, String> contextOf(UUID uuid) {
        Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player == null) {
            return Map.of();
        }
        return Map.of("world", player.getWorld().getName(),
                "gamemode", player.getGameMode().name().toLowerCase(Locale.ROOT));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
