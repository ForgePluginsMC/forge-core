package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** /gm — change gamemode for yourself or another player. */
public final class GmCommand extends ForgeCommand {
    public GmCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "gm";
    }

    @Override
    public List<String> aliases() {
        return List.of("gamemode");
    }

    @Override
    public String description() {
        return "Change gamemode.";
    }

    @Override
    public String usage() {
        return "/gm <0|1|2|3|survival|creative|adventure|spectator> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        GameMode mode = parse(args[0]);
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("forgecore.gm.others")) {
                throw new CommandFailure("You don't have permission to change other players' gamemode.");
            }
            target = Players.find(sender, args[1]);
            if (target == null) {
                return;
            }
        } else {
            target = asPlayer(sender);
            if (target == null) {
                Text.usage(sender, usage());
                return;
            }
        }
        target.setGameMode(mode);
        Text.ok(sender, "Gamemode set to <white>" + mode.name().toLowerCase(Locale.ROOT)
                + "</white> for <white>" + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "Your gamemode was set to <white>"
                    + mode.name().toLowerCase(Locale.ROOT) + "</white>.");
        }
    }

    private static GameMode parse(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> throw new CommandFailure("Unknown gamemode: " + input);
        };
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(
                    List.of("survival", "creative", "adventure", "spectator"), args);
        }
        if (args.length == 2 && sender.hasPermission("forgecore.gm.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
