package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle patrol mode: hop between online players every 10 seconds. */
public final class PatrolCommand extends ForgeCommand {
    public PatrolCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "patrol";
    }

    @Override
    public String description() {
        return "Toggle patrol mode (visit every online player, 10s apart).";
    }

    @Override
    public String usage() {
        return "/patrol";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        boolean now = ModerationSetup.patrol().toggle(player);
        Text.ok(sender, now ? "Patrol mode enabled." : "Patrol mode disabled.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
