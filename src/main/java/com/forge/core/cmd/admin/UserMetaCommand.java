package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * /usermeta — raw read/write access to a player's userdata file.
 * Console-friendly; works for offline players.
 */
public final class UserMetaCommand extends ForgeCommand {
    public UserMetaCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "usermeta";
    }

    @Override
    public String description() {
        return "Get, set, remove or list raw userdata keys.";
    }

    @Override
    public String usage() {
        return "/usermeta <player> <get|set|remove|list> [key] [value]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid = resolveUuid(args[0]);
        if (uuid == null) {
            throw new CommandFailure("No userdata for player: " + args[0]);
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "get" -> get(sender, uuid, requireKey(args));
            case "set" -> set(sender, uuid, args);
            case "remove" -> remove(sender, uuid, requireKey(args));
            case "list" -> list(sender, uuid);
            default -> Text.usage(sender, usage());
        }
    }

    private void get(CommandSender sender, UUID uuid, String key) {
        UserData data = plugin.users().get(uuid);
        String value = data.getString(key, null);
        if (value == null) {
            Text.send(sender, "<gray>Key <white>" + Text.escape(key) + "</white> is not set.</gray>");
        } else {
            Text.send(sender, "<white>" + Text.escape(key) + "</white> = <yellow>"
                    + Text.escape(value) + "</yellow>");
        }
    }

    private void set(CommandSender sender, UUID uuid, String[] args) {
        if (args.length < 4) {
            Text.usage(sender, usage());
            return;
        }
        String key = args[2];
        String raw = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
        UserData data = plugin.users().get(uuid);
        data.set(key, parseValue(raw));
        plugin.users().save(uuid);
        Text.ok(sender, "Set <white>" + Text.escape(key) + "</white>.");
    }

    private void remove(CommandSender sender, UUID uuid, String key) {
        UserData data = plugin.users().get(uuid);
        data.set(key, null);
        plugin.users().save(uuid);
        Text.ok(sender, "Removed <white>" + Text.escape(key) + "</white>.");
    }

    private void list(CommandSender sender, UUID uuid) {
        // Key enumeration reads the on-disk file; in-memory dirty values are
        // flushed first so the listing is current.
        plugin.users().saveAll();
        File file = new File(new File(plugin.getDataFolder(), "userdata"), uuid + ".yml");
        if (!file.exists()) {
            Text.send(sender, "<gray>No userdata file for that player.</gray>");
            return;
        }
        List<String> keys = new ArrayList<>(
                YamlConfiguration.loadConfiguration(file).getKeys(true));
        if (keys.isEmpty()) {
            Text.send(sender, "<gray>Userdata is empty.</gray>");
            return;
        }
        Text.send(sender, "<gold>Userdata keys (" + keys.size() + "):</gold>");
        for (String key : keys.subList(0, Math.min(keys.size(), 40))) {
            Text.send(sender, "  <gray>•</gray> <white>" + Text.escape(key) + "</white>");
        }
        if (keys.size() > 40) {
            Text.send(sender, "  <gray>…and " + (keys.size() - 40) + " more.</gray>");
        }
    }

    private static String requireKey(String[] args) {
        if (args.length < 3) {
            throw new CommandFailure("Specify a key.");
        }
        return args[2];
    }

    /** Smart-parse a value: boolean → long → double → string. */
    private static Object parseValue(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.equals("true")) {
            return true;
        }
        if (lower.equals("false")) {
            return false;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException notLong) {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException notDouble) {
                return raw;
            }
        }
    }

    private static UUID resolveUuid(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        OfflinePlayer offline = Players.offline(name);
        return offline == null ? null : offline.getUniqueId();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(List.of("get", "set", "remove", "list"), args);
        }
        return List.of();
    }
}
