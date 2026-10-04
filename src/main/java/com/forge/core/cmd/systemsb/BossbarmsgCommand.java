package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show a timed boss bar message to a player or everyone. */
public final class BossbarmsgCommand extends ForgeCommand {
    public BossbarmsgCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "bossbarmsg";
    }

    @Override
    public String description() {
        return "Show a timed boss bar message.";
    }

    @Override
    public String usage() {
        return "/bossbarmsg <player|*> <seconds> <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            Text.usage(sender, usage());
            return;
        }
        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Seconds must be a number.");
        }
        if (seconds < 1 || seconds > 300) {
            throw new CommandRegistry.CommandFailure("Seconds must be between 1 and 300.");
        }
        List<Player> targets;
        if (args[0].equals("*")) {
            targets = new ArrayList<>(Bukkit.getOnlinePlayers());
        } else {
            Player target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
            targets = List.of(target);
        }
        if (targets.isEmpty()) {
            throw new CommandRegistry.CommandFailure("Nobody is online.");
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        for (Player target : targets) {
            SystemsBSetup.bossbars().show(
                    target,
                    Text.of(Placeholders.apply(target, message)),
                    BossBar.Color.WHITE,
                    seconds);
        }
        Text.ok(sender, "Boss bar shown to " + targets.size() + " player(s).");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>(Players.onlineNames());
            names.add("*");
            return Players.filter(names, args);
        }
        return List.of();
    }
}
