package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.PermissionEntry;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** Grant a permission node directly to a player. */
public final class PermSetCommand extends ForgeCommand {
    public PermSetCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "permset";
    }

    @Override
    public String description() {
        return "Grant a permission node to a player.";
    }

    @Override
    public String usage() {
        return "/permset <player> <node> [true|false]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2 || args.length > 3) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, args[0]);
        if (uuid == null) {
            return;
        }
        boolean value = true;
        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("deny")) {
                value = false;
            } else if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("allow")) {
                Text.error(sender, "Value must be true or false.");
                return;
            }
        }
        String node = args[1].toLowerCase(Locale.ROOT);
        plugin.permissions().setPermission(uuid,
                new PermissionEntry(node, value, Map.of(), 0));
        Text.ok(sender, (value ? "Granted <white>" : "Denied <white>")
                + Text.escape(node) + "</white> " + (value ? "to" : "for")
                + " <white>" + Text.escape(PermUtil.displayName(uuid)) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 3) {
            return Players.filter(List.of("true", "false"), args);
        }
        return List.of();
    }
}
