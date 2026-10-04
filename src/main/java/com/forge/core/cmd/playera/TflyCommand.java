package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle flight for another player. */
public final class TflyCommand extends PlayerACommand {
    public TflyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tfly";
    }

    @Override
    public String description() {
        return "Toggle flight for another player.";
    }

    @Override
    public String usage() {
        return "/tfly <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = requiredTarget(sender, args);
        if (target == null) {
            return;
        }
        boolean fly = !target.getAllowFlight();
        target.setAllowFlight(fly);
        if (!fly) {
            target.setFlying(false);
        }
        String state = fly ? "<green>enabled</green>" : "<red>disabled</red>";
        Text.ok(sender, "Flight " + state + "<green> for <white>" + Text.escape(target.getName()) + "</white>.");
        Text.send(target, "Your flight was " + state + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
