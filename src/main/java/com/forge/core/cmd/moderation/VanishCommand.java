package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle vanish for yourself. */
public final class VanishCommand extends ForgeCommand {
    public VanishCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "vanish";
    }

    @Override
    public String description() {
        return "Toggle vanish (invisible to players without forgecore.vanish.see).";
    }

    @Override
    public String usage() {
        return "/vanish";
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
        boolean now = ModerationSetup.vanish().toggle(player);
        Text.ok(sender, now ? "You are now vanished." : "You are no longer vanished.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
