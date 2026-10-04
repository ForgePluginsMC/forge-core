package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * /migratedatabase — scan the ForgeCore data folder for legacy data formats
 * and convert them. Reports honestly when there is nothing to migrate.
 */
public final class MigrateDatabaseCommand extends ForgeCommand {
    /** Filenames from older storage layouts that this command knows how to read. */
    private static final List<String> LEGACY_FILES = List.of(
            "players.yml", "data.yml", "homes.yml", "users.yml",
            "warps-legacy.yml", "kits-legacy.yml", "economy-legacy.yml");

    public MigrateDatabaseCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "migratedatabase";
    }

    @Override
    public String description() {
        return "Scan for legacy ForgeCore data formats and convert them.";
    }

    @Override
    public String usage() {
        return "/migratedatabase";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        File dataFolder = plugin.getDataFolder();
        List<String> found = new ArrayList<>();
        for (String name : LEGACY_FILES) {
            if (new File(dataFolder, name).exists()) {
                found.add(name);
            }
        }
        File legacyDir = new File(dataFolder, "legacy");
        if (legacyDir.isDirectory()) {
            String[] children = legacyDir.list();
            if (children != null) {
                for (String child : children) {
                    found.add("legacy/" + child);
                }
            }
        }
        Text.send(sender, "Scanned <white>" + (LEGACY_FILES.size() + 1) + "</white> legacy locations.");
        if (found.isEmpty()) {
            Text.ok(sender, "No legacy data found — nothing to migrate.");
            return;
        }
        // ForgeCore 1.x has no older on-disk schema to convert from; anything
        // found here is reported so an admin can inspect it manually.
        Text.send(sender, "<yellow>Found " + found.size() + " legacy file(s):</yellow>");
        for (String name : found) {
            Text.send(sender, "  <gray>•</gray> <white>" + Text.escape(name) + "</white>");
        }
        Text.send(sender, "<gray>ForgeCore 1.x has no older schema to convert these from; "
                + "they were left untouched. Use /importfrom or /importoldusers for external data.</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
