package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Delete userdata files whose last activity is older than the given number
 * of days. Uses the "last-seen" timestamp when present, otherwise the file's
 * modification time. Online players are never purged.
 */
public final class PurgeCommand extends ForgeCommand {
    public PurgeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "purge";
    }

    @Override
    public String description() {
        return "Delete userdata of players inactive for the given days.";
    }

    @Override
    public String usage() {
        return "/purge <days>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        long days;
        try {
            days = Long.parseLong(args[0]);
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Days must be a number.");
        }
        if (days < 1) {
            throw new CommandRegistry.CommandFailure("Days must be at least 1.");
        }
        File folder = new File(plugin.getDataFolder(), "userdata");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            Text.send(sender, "No userdata to purge.");
            return;
        }
        long cutoff = System.currentTimeMillis() - days * 86_400_000L;
        int purged = 0;
        for (File file : files) {
            UUID uuid;
            try {
                uuid = UUID.fromString(file.getName().replace(".yml", ""));
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                continue;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            long lastSeen = config.getLong("last-seen", 0L);
            long ageBase = lastSeen > 0 ? lastSeen : file.lastModified();
            if (ageBase < cutoff) {
                plugin.users().unload(uuid);
                if (file.delete()) {
                    purged++;
                }
            }
        }
        Text.ok(sender, "Purged <white>" + purged + "</white> inactive player file(s) (older than <white>" + days + "d</white>).");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("30", "90", "180", "365");
        }
        return List.of();
    }
}
