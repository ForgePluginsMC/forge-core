package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle seeing the commands other players run. */
public final class CommandspyCommand extends ForgeCommand {
    public CommandspyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "commandspy";
    }

    @Override
    public String description() {
        return "Toggle seeing the commands other players run.";
    }

    @Override
    public String usage() {
        return "/commandspy";
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
        boolean now = !plugin.mutes().commandSpy(player);
        plugin.mutes().setCommandSpy(player, now);
        Text.ok(sender, now ? "Command spy enabled." : "Command spy disabled.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
