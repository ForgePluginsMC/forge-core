package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /removehome <name> — delete one of your homes. */
public final class RemoveHomeCommand extends TeleportCommand {
    public RemoveHomeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "removehome";
    }

    @Override
    public List<String> aliases() {
        return List.of("delhome");
    }

    @Override
    public String description() {
        return "Delete one of your homes.";
    }

    @Override
    public String usage() {
        return "/removehome <name>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        if (plugin.users().get(player).removeHome(args[0])) {
            plugin.users().save(player.getUniqueId());
            Text.ok(sender, "Home <white>" + Text.escape(args[0]) + "</white> removed.");
        } else {
            throw fail("Home <white>" + Text.escape(args[0]) + "</white> does not exist.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return Players.filter(new ArrayList<>(plugin.users().get(player).homes().keySet()), args);
        }
        return List.of();
    }
}
