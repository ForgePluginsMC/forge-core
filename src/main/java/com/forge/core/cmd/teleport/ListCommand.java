package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.forge.core.vanish.VanishApi;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /list — show online count and player names (vanished hidden without permission). */
public final class ListCommand extends TeleportCommand {
    public ListCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public List<String> aliases() {
        return List.of("who", "online");
    }

    @Override
    public String description() {
        return "List online players.";
    }

    @Override
    public String usage() {
        return "/list";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        boolean seeVanished = sender.hasPermission("forgecore.vanish.see");
        List<String> names = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (VanishApi.isVanished(player) && !seeVanished) {
                continue;
            }
            names.add(player.getName());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        int max = plugin.getServer().getMaxPlayers();
        if (names.isEmpty()) {
            Text.send(sender, "<gray>Online (<white>0/" + max + "</white>): nobody.");
            return;
        }
        Text.send(sender, "<gray>Online (<white>" + names.size() + "/" + max + "</white>): <white>"
                + Text.escape(String.join("<gray>, <white>", names)));
    }
}
