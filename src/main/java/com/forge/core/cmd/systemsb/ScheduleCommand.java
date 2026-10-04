package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.schedule.ScheduleManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Inspect and force-run declarative schedules. */
public final class ScheduleCommand extends ForgeCommand {
    public ScheduleCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "schedule";
    }

    @Override
    public String description() {
        return "List, inspect and run schedules.";
    }

    @Override
    public String usage() {
        return "/schedule <list|run|info> [name]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        ScheduleManager schedules = SystemsBSetup.schedules();
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> {
                List<ScheduleManager.ScheduleDef> all = schedules.all();
                if (all.isEmpty()) {
                    Text.send(sender, "<gray>No schedules defined.");
                    return;
                }
                Text.send(sender, "<gold><bold>Schedules:");
                for (ScheduleManager.ScheduleDef def : all) {
                    Text.send(sender, " <white>" + Text.escape(def.name()) + "</white> <gray>("
                            + Text.escape(def.trigger().name().toLowerCase(Locale.ROOT)) + ")");
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    Text.usage(sender, usage());
                    return;
                }
                ScheduleManager.ScheduleDef def = schedules.byName(args[1]);
                if (def == null) {
                    throw new CommandRegistry.CommandFailure(
                            "Unknown schedule: " + Text.escape(args[1]) + ".");
                }
                Text.send(sender, "<gold><bold>Schedule: <white>" + Text.escape(def.name()));
                Text.send(sender, "<gray>Trigger: <white>"
                        + Text.escape(def.trigger().name().toLowerCase(Locale.ROOT)));
                if (def.trigger() == ScheduleManager.Trigger.INTERVAL
                        || def.trigger() == ScheduleManager.Trigger.PLAYTIME) {
                    Text.send(sender, "<gray>Every: <white>" + def.minutes() + " minutes");
                }
                Text.send(sender, "<gray>Actions: <white>" + def.actions().size());
            }
            case "run" -> {
                if (!sender.hasPermission("forgecore.schedule.run")) {
                    throw new CommandRegistry.CommandFailure("You don't have permission to run schedules.");
                }
                if (args.length < 2) {
                    Text.usage(sender, usage());
                    return;
                }
                ScheduleManager.ScheduleDef def = schedules.byName(args[1]);
                if (def == null) {
                    throw new CommandRegistry.CommandFailure(
                            "Unknown schedule: " + Text.escape(args[1]) + ".");
                }
                Player player = asPlayer(sender);
                schedules.run(def, player);
                Text.ok(sender, "Ran schedule <white>" + Text.escape(def.name()) + "</white>.");
            }
            default -> Text.usage(sender, usage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("list", "run", "info"), args);
        }
        if (args.length == 2
                && (args[0].equalsIgnoreCase("run") || args[0].equalsIgnoreCase("info"))) {
            List<String> names = new java.util.ArrayList<>();
            for (ScheduleManager.ScheduleDef def : SystemsBSetup.schedules().all()) {
                names.add(def.name());
            }
            return Players.filter(names, args);
        }
        return List.of();
    }
}
