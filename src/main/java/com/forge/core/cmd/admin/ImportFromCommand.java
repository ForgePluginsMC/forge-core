package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * /importfrom — import player data, warps and kits from another plugin's
 * data files. Currently supports {@code essentials} (EssentialsX).
 *
 * <p>All parsing below is original code written against the documented
 * on-disk YAML layout; no Essentials code is used or copied.
 */
public final class ImportFromCommand extends ForgeCommand {
    private static final Pattern COLOR_CODE = Pattern.compile("§.");

    public ImportFromCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "importfrom";
    }

    @Override
    public String description() {
        return "Import data from another plugin (essentials).";
    }

    @Override
    public String usage() {
        return "/importfrom <essentials>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        if (!args[0].equalsIgnoreCase("essentials")) {
            throw new CommandFailure("Unknown source: " + args[0] + ". Supported: essentials.");
        }
        File serverRoot = plugin.getDataFolder().getParentFile().getParentFile();
        File essentials = new File(serverRoot, "plugins/Essentials");
        if (!essentials.isDirectory()) {
            throw new CommandFailure("Essentials data folder not found (plugins/Essentials).");
        }

        int players = 0;
        int homes = 0;
        homes += importUserdata(new File(essentials, "userdata"));
        players = lastPlayerCount;
        int warps = importWarps(new File(essentials, "warps"));
        KitImport kits = importKits(new File(essentials, "kits.yml"));

        plugin.users().saveAll();
        plugin.economy().save();
        plugin.warps().save();
        plugin.kits().save();

        Text.ok(sender, "Essentials import complete:");
        Text.send(sender, "  Players: <white>" + players + "</white>");
        Text.send(sender, "  Homes: <white>" + homes + "</white>");
        Text.send(sender, "  Warps: <white>" + warps + "</white>");
        Text.send(sender, "  Kits: <white>" + kits.imported() + "</white>"
                + (kits.skippedItems() > 0
                        ? " <gray>(" + kits.skippedItems() + " complex kit items skipped)</gray>" : ""));
    }

    private int lastPlayerCount;

    /** Import plugins/Essentials/userdata/*.yml. Returns homes imported. */
    private int importUserdata(File folder) {
        if (!folder.isDirectory()) {
            return 0;
        }
        int homes = 0;
        int players = 0;
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return 0;
        }
        for (File file : files) {
            UUID uuid;
            try {
                uuid = UUID.fromString(file.getName().replace(".yml", ""));
            } catch (IllegalArgumentException bad) {
                continue;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            UserData data = plugin.users().get(uuid);
            boolean touched = false;

            double money = readDouble(config, "money");
            if (!Double.isNaN(money)) {
                plugin.economy().set(uuid, money);
                touched = true;
            }
            String nickname = config.getString("nickname", null);
            if (nickname != null && !nickname.isBlank()) {
                data.setNick(COLOR_CODE.matcher(nickname).replaceAll(""));
                touched = true;
            }
            ConfigurationSection homesSection = config.getConfigurationSection("homes");
            if (homesSection != null) {
                for (String homeName : homesSection.getKeys(false)) {
                    Location location = readEssentialsLocation(homesSection.getConfigurationSection(homeName));
                    if (location != null) {
                        data.setHome(homeName, location);
                        homes++;
                        touched = true;
                    }
                }
            }
            if (touched) {
                players++;
            }
        }
        lastPlayerCount = players;
        return homes;
    }

    /** Import plugins/Essentials/warps/*.yml. Returns warps imported. */
    private int importWarps(File folder) {
        if (!folder.isDirectory()) {
            return 0;
        }
        int count = 0;
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return 0;
        }
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = config.getString("name",
                    file.getName().replace(".yml", "")).toLowerCase(Locale.ROOT);
            Location location = readEssentialsLocation(config);
            if (location != null) {
                plugin.warps().set(name, location);
                count++;
            }
        }
        return count;
    }

    private record KitImport(int imported, int skippedItems) {
    }

    /** Import plugins/Essentials/kits.yml. Best-effort item parsing. */
    private KitImport importKits(File file) {
        if (!file.exists()) {
            return new KitImport(0, 0);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection kits = config.getConfigurationSection("kits");
        if (kits == null) {
            return new KitImport(0, 0);
        }
        int imported = 0;
        int skipped = 0;
        for (String kitName : kits.getKeys(false)) {
            ConfigurationSection section = kits.getConfigurationSection(kitName);
            if (section == null) {
                continue;
            }
            long delay = section.getLong("delay", 0);
            List<ItemStack> items = new ArrayList<>();
            for (String entry : section.getStringList("items")) {
                ItemStack item = parseSimpleItem(entry);
                if (item == null) {
                    skipped++;
                } else {
                    items.add(item);
                }
            }
            plugin.kits().create(kitName.toLowerCase(Locale.ROOT), items, delay, 0.0);
            imported++;
        }
        return new KitImport(imported, skipped);
    }

    /**
     * Parse simple Essentials kit entries like {@code "diamond 1"} or
     * {@code "bread"}. Complex entries (enchantments, meta, …) return null
     * and are counted as skipped.
     */
    private static ItemStack parseSimpleItem(String entry) {
        String[] parts = entry.trim().split("\\s+");
        if (parts.length == 0 || parts.length > 2 || parts[0].contains(":")) {
            return null;
        }
        Material material = Material.matchMaterial(parts[0]);
        if (material == null || material.isAir() || !material.isItem()) {
            return null;
        }
        int amount = 1;
        if (parts.length == 2) {
            try {
                amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException bad) {
                return null;
            }
            if (amount < 1 || amount > material.getMaxStackSize()) {
                return null;
            }
        }
        return new ItemStack(material, amount);
    }

    /** Read an Essentials-style {world, x, y, z, yaw, pitch} section. */
    private static Location readEssentialsLocation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String worldName = section.getString("world", "");
        World world = Bukkit.getWorld(worldName);
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

    /** Read a value that may be stored as a number or a numeric string. */
    private static double readDouble(YamlConfiguration config, String path) {
        Object raw = config.get(path);
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        if (raw instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException bad) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(List.of("essentials"), args);
        }
        return List.of();
    }
}
