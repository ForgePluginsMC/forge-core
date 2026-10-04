package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Set personal client-side weather, or reset it. */
public final class PweatherCommand extends PlayerACommand {
    public PweatherCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "pweather";
    }

    @Override
    public String description() {
        return "Set your personal weather.";
    }

    @Override
    public String usage() {
        return "/pweather <clear|rain|reset> [player]";
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
        String input = args[0].toLowerCase(Locale.ROOT);
        switch (input) {
            case "reset" -> {
                target.resetPlayerWeather();
                Text.ok(sender, "Personal weather reset for <white>" + Text.escape(target.getName()) + "</white>.");
            }
            case "clear", "sun", "sunny" -> {
                target.setPlayerWeather(WeatherType.CLEAR);
                Text.ok(sender, "Personal weather set to <white>clear</white> for <white>"
                        + Text.escape(target.getName()) + "</white>.");
            }
            case "rain", "storm", "downfall" -> {
                target.setPlayerWeather(WeatherType.DOWNFALL);
                Text.ok(sender, "Personal weather set to <white>rain</white> for <white>"
                        + Text.escape(target.getName()) + "</white>.");
            }
            default -> Text.error(sender, "Use clear, rain, or reset.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("clear", "rain", "reset"), args);
        }
        return playerNames(args);
    }
}
