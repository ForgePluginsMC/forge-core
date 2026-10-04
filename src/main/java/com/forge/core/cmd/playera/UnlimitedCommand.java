package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle unlimited block placement. */
public final class UnlimitedCommand extends PlayerACommand {
    public UnlimitedCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unlimited";
    }

    @Override
    public List<String> aliases() {
        return List.of("unlim");
    }

    @Override
    public String description() {
        return "Toggle unlimited block placement.";
    }

    @Override
    public String usage() {
        return "/unlimited";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        boolean now = PlayerAState.unlimited().toggle(player.getUniqueId());
        Text.ok(sender, "Unlimited blocks " + (now ? "enabled" : "disabled") + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
