package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle seeing other players' private messages. */
public final class SocialspyCommand extends ForgeCommand {
    public SocialspyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "socialspy";
    }

    @Override
    public String description() {
        return "Toggle seeing other players' private messages.";
    }

    @Override
    public String usage() {
        return "/socialspy";
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
        boolean now = !plugin.mutes().socialSpy(player);
        plugin.mutes().setSocialSpy(player, now);
        Text.ok(sender, now ? "Social spy enabled." : "Social spy disabled.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
