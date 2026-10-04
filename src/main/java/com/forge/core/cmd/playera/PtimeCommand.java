package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Set personal client-side time (named times or ticks), or reset it. */
public final class PtimeCommand extends PlayerACommand {
    public PtimeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ptime";
    }

    @Override
    public String description() {
        return "Set your personal time.";
    }

    @Override
    public String usage() {
        return "/ptime <day|noon|night|midnight|<ticks>|reset> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player target = targetAt(sender, args, 1);
        if (target == null) {
            return;
        }
        String input = args[0];
        if (input.equalsIgnoreCase("reset")) {
            target.resetPlayerTime();
            Text.ok(sender, "Personal time reset for <white>" + Text.escape(target.getName()) + "</white>.");
            return;
        }
        Long ticks = parse(input);
        if (ticks == null) {
            Text.error(sender, "Unknown time <white>" + Text.escape(input)
                    + "</white>. Use day, noon, night, midnight, ticks, or reset.");
            return;
        }
        target.setPlayerTime(ticks, false);
        Text.ok(sender, "Personal time set to <white>" + ticks + "</white> for <white>"
                + Text.escape(target.getName()) + "</white>.");
    }

    private static @Nullable Long parse(String input) {
        switch (input.toLowerCase(Locale.ROOT)) {
            case "day", "morning" -> {
                return 1000L;
            }
            case "sunrise" -> {
                return 23000L;
            }
            case "noon" -> {
                return 6000L;
            }
            case "sunset" -> {
                return 12000L;
            }
            case "night" -> {
                return 13000L;
            }
            case "midnight" -> {
                return 18000L;
            }
            default -> {
                try {
                    long ticks = Long.parseLong(input);
                    return ticks >= 0 && ticks <= 24000 ? ticks : null;
                } catch (NumberFormatException exception) {
                    return null;
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("day", "noon", "sunset", "night", "midnight", "sunrise", "reset"), args);
        }
        return playerNames(args);
    }
}
