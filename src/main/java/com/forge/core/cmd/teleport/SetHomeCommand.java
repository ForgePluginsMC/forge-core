package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /sethome <name> — save your current location as a home. */
public final class SetHomeCommand extends TeleportCommand {
    public SetHomeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sethome";
    }

    @Override
    public String description() {
        return "Set a home at your current location.";
    }

    @Override
    public String usage() {
        return "/sethome <name>";
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
        String homeName = args[0].toLowerCase(java.util.Locale.ROOT);
        UserData data = plugin.users().get(player);
        boolean isNew = !data.homes().containsKey(homeName);
        if (isNew && !player.hasPermission("forgecore.home.unlimited")) {
            int max = plugin.getConfig().getInt("max-homes", 5);
            if (data.homes().size() >= max) {
                throw fail("You already have the maximum of " + max + " homes.");
            }
        }
        data.setHome(homeName, player.getLocation());
        plugin.users().save(player.getUniqueId());
        Text.ok(sender, "Home <white>" + Text.escape(homeName) + "</white> set.");
    }
}
