package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Clear chat for every online player. */
public final class ClearchatCommand extends ForgeCommand {
    public ClearchatCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "clearchat";
    }

    @Override
    public String description() {
        return "Clear chat for all players.";
    }

    @Override
    public String usage() {
        return "/clearchat";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int line = 0; line < 100; line++) {
                player.sendMessage(Component.empty());
            }
            Text.send(player, "Chat was cleared by <white>" + Text.escape(sender.getName()) + "</white>.");
        }
        Text.ok(sender, "Chat cleared for <white>" + Bukkit.getOnlinePlayers().size() + "</white> player(s).");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
