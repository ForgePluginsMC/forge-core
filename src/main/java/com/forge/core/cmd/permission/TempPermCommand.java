package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.PermissionEntry;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** Grant a player a temporary permission node. */
public final class TempPermCommand extends ForgeCommand {
    public TempPermCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tempperm";
    }

    @Override
    public String description() {
        return "Give a player a permission node for a limited time.";
    }

    @Override
    public String usage() {
        return "/tempperm <player> <node> <duration>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, args[0]);
        if (uuid == null) {
            return;
        }
        long seconds;
        try {
            seconds = Time.parseSeconds(args[2]);
        } catch (IllegalArgumentException e) {
            throw new CommandRegistry.CommandFailure("Bad duration. Examples: 30m, 2h, 7d.");
        }
        PermissionEntry parsed = PermissionEntry.parse(args[1]);
        long expiry = System.currentTimeMillis() + seconds * 1000L;
        plugin.permissions().setPermission(uuid,
                new PermissionEntry(parsed.node(), parsed.value(), Map.of(), expiry));
        Text.ok(sender, "Gave <white>" + Text.escape(PermUtil.displayName(uuid)) + "</white> node <white>"
                + Text.escape(parsed.display()) + "</white> for <white>" + Time.format(seconds) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 3) {
            return Players.filter(List.of("30m", "1h", "1d", "7d", "30d"), args);
        }
        return List.of();
    }
}
