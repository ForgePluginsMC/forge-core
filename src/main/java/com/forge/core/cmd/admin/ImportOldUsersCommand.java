package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * /importoldusers — import userdata-style YAML files from a folder into
 * ForgeCore userdata. Each {@code *.yml} is one player: the file name is a
 * UUID or a player name. Recognized keys: {@code homes} (Essentials-style
 * {world,x,y,z,yaw,pitch} maps), {@code money}, {@code nick}/{@code nickname}.
 */
public final class ImportOldUsersCommand extends ForgeCommand {
    private static final Pattern COLOR_CODE = Pattern.compile("§.");

    public ImportOldUsersCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "importoldusers";
    }

    @Override
    public String description() {
        return "Import userdata-style yml files from a folder.";
    }

    @Override
    public String usage() {
        return "/importoldusers <folder>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        File folder = new File(args[0]);
        if (!folder.isAbsolute()) {
            folder = new File(plugin.getDataFolder().getParentFile().getParentFile(), args[0]);
        }
        if (!folder.isDirectory()) {
            throw new CommandFailure("Not a folder: " + args[0]);
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            throw new CommandFailure("No .yml files in " + folder.getPath());
        }

        int imported = 0;
        int homes = 0;
        int skipped = 0;
        for (File file : files) {
            UUID uuid = resolveUuid(file.getName().replace(".yml", ""));
            if (uuid == null) {
                skipped++;
                continue;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            UserData data = plugin.users().get(uuid);
            boolean touched = false;

            Object moneyRaw = config.get("money");
            if (moneyRaw instanceof Number number) {
                plugin.economy().set(uuid, number.doubleValue());
                touched = true;
            }
            String nick = config.getString("nick", config.getString("nickname", null));
            if (nick != null && !nick.isBlank()) {
                data.setNick(COLOR_CODE.matcher(nick).replaceAll(""));
                touched = true;
            }
            ConfigurationSection homesSection = config.getConfigurationSection("homes");
            if (homesSection != null) {
                for (String homeName : homesSection.getKeys(false)) {
                    Location location = readLocation(homesSection.getConfigurationSection(homeName));
                    if (location != null) {
                        data.setHome(homeName, location);
                        homes++;
                        touched = true;
                    }
                }
            }
            if (touched) {
                imported++;
            } else {
                skipped++;
            }
        }
        plugin.users().saveAll();
        plugin.economy().save();
        Text.ok(sender, "Imported <white>" + imported + "</white> users (<white>" + homes
                + "</white> homes), skipped <white>" + skipped + "</white>.");
    }

    /** File name → UUID, either directly or via an offline player lookup. */
    private static UUID resolveUuid(String name) {
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException notUuid) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            return offline.hasPlayedBefore() ? offline.getUniqueId() : null;
        }
    }

    private static Location readLocation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        World world = Bukkit.getWorld(section.getString("world", ""));
        if (world == null) {
            return null;
        }
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("world", world.getUID().toString());
            map.put("x", section.getDouble("x"));
            map.put("y", section.getDouble("y"));
            map.put("z", section.getDouble("z"));
            map.put("yaw", (float) section.getDouble("yaw"));
            map.put("pitch", (float) section.getDouble("pitch"));
            return Locs.deserialize(map);
        } catch (RuntimeException bad) {
            return null;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
