package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle sign-creation monitoring. */
public final class SignspyCommand extends ForgeCommand {
    public SignspyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "signspy";
    }

    @Override
    public String description() {
        return "Toggle monitoring of sign creation.";
    }

    @Override
    public String usage() {
        return "/signspy";
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
        boolean on = ModerationState.signSpy().toggle(player.getUniqueId());
        Text.ok(sender, "SignSpy " + (on ? "<green>enabled</green>." : "<red>disabled</red>."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
