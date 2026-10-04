package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /home [name] — teleport to one of your homes (default "home"). */
public final class HomeCommand extends TeleportCommand {
    public HomeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "home";
    }

    @Override
    public String description() {
        return "Teleport to one of your homes.";
    }

    @Override
    public String usage() {
        return "/home [name]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        String homeName = args.length > 0 ? args[0] : "home";
        Location home = plugin.users().get(player).homes().get(homeName.toLowerCase(java.util.Locale.ROOT));
        if (home == null) {
            throw fail("Home <white>" + Text.escape(homeName) + "</white> does not exist. Use /sethome first.");
        }
        teleport(player, home, "<gray>Teleported to home <white>" + Text.escape(homeName) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return Players.filter(new ArrayList<>(plugin.users().get(player).homes().keySet()), args);
        }
        return List.of();
    }
}
