package com.forge.core.merge.announcer;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;

/**
 * {@code /announce} — manage the ForgeCore announcer: list configured
 * announcements, reload {@code announcer.yml}, or broadcast one immediately
 * by id.
 */
final class AnnounceCommand extends ForgeCommand {

    AnnounceCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "announce";
    }

    @Override
    public List<String> aliases() {
        return List.of("fannouncer");
    }

    @Override
    public String description() {
        return "Manage scheduled announcements (list, reload, broadcast).";
    }

    @Override
    public String usage() {
        return "/announce <list|reload|broadcast <id>>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        AnnouncerManager manager = AnnouncerSetup.manager();
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Missing subcommand.", usage());
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                manager.reload();
                Text.ok(sender, "Announcer reloaded: <white>" + manager.count() + "</white> announcement(s) active.");
            }
            case "list" -> {
                List<String> ids = manager.ids();
                if (ids.isEmpty()) {
                    Text.send(sender, "<yellow>No announcements configured.</yellow>");
                } else {
                    Text.send(sender, "<green>Announcements: <white>" + String.join("<gray>, </gray><white>", ids) + "</white></green>");
                }
            }
            case "broadcast" -> {
                if (args.length < 2) {
                    throw new CommandRegistry.CommandFailure("Missing announcement id.", "/announce broadcast <id>");
                }
                if (manager.broadcastNow(args[1])) {
                    Text.ok(sender, "Broadcasted announcement <white>" + Text.escape(args[1]) + "</white>.");
                } else {
                    throw new CommandRegistry.CommandFailure(
                            "Unknown announcement id: <white>" + Text.escape(args[1]) + "</white>.");
                }
            }
            default -> throw new CommandRegistry.CommandFailure(
                    "Unknown subcommand: <white>" + Text.escape(args[0]) + "</white>.", usage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("list", "reload", "broadcast"), args);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("broadcast")) {
            return Players.filter(AnnouncerSetup.manager().ids(), args);
        }
        return List.of();
    }
}
