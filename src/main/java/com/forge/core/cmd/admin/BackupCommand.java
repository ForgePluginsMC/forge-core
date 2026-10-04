package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bukkit.command.CommandSender;

/**
 * One-command server backup: zips world folders and the plugin data folder
 * into {@code backups/}. Runs async so the server doesn't lag.
 */
public final class BackupCommand extends ForgeCommand {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public BackupCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "backup";
    }

    @Override
    public String description() {
        return "Back up worlds and plugin data to a zip file.";
    }

    @Override
    public String usage() {
        return "/backup";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Text.send(sender, "Backup started in the background...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Path root = plugin.getServer().getWorldContainer().toPath();
                Path backups = root.resolve("backups");
                Files.createDirectories(backups);
                String stamp = LocalDateTime.now().format(FORMAT);
                Path zip = backups.resolve("backup-" + stamp + ".zip");
                try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
                    // World folders.
                    for (var world : plugin.getServer().getWorlds()) {
                        Path worldDir = world.getWorldFolder().toPath();
                        zipTree(out, root, worldDir);
                    }
                    // Plugin data (configs, not jars).
                    Path plugins = root.resolve("plugins");
                    if (Files.isDirectory(plugins)) {
                        try (var stream = Files.list(plugins)) {
                            for (Path child : stream.toList()) {
                                if (Files.isDirectory(child) && !child.getFileName().toString().equals("backups")) {
                                    zipTree(out, root, child);
                                }
                            }
                        }
                    }
                }
                long sizeMb = Files.size(zip) / 1024 / 1024;
                plugin.getServer().getScheduler().runTask(plugin, () -> Text.ok(sender,
                        "Backup complete: <white>" + zip.getFileName() + "</white> (" + sizeMb + " MB)."));
            } catch (IOException exception) {
                plugin.getServer().getScheduler().runTask(plugin, () -> Text.error(sender,
                        "Backup failed: " + Text.escape(exception.getMessage())));
            }
        });
    }

    private static void zipTree(ZipOutputStream out, Path root, Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                // Skip live lock/session files that change constantly.
                if (name.equals("session.lock") || name.endsWith(".tmp")) {
                    return FileVisitResult.CONTINUE;
                }
                out.putNextEntry(new ZipEntry(root.relativize(file).toString()));
                try (InputStream in = Files.newInputStream(file)) {
                    in.transferTo(out);
                }
                out.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
