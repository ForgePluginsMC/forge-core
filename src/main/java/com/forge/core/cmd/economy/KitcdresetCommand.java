package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

/** Clear a player's kit cooldowns. */
public final class KitcdresetCommand extends ForgeCommand {
    public KitcdresetCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "kitcdreset";
    }

    @Override
    public String description() {
        return "Reset a player's kit cooldowns.";
    }

    @Override
    public String usage() {
        return "/kitcdreset <player> [kit]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            Text.usage(sender, usage());
            return;
        }
        @Nullable OfflinePlayer target = Players.offline(args[0]);
        if (target == null) {
            throw new CommandRegistry.CommandFailure(
                    "Player <white>" + Text.escape(args[0]) + "</white> has never played here.");
        }
        UserData data = plugin.users().get(target.getUniqueId());
        String targetName = target.getName() == null ? args[0] : target.getName();
        if (args.length == 2) {
            String kit = args[1].toLowerCase(Locale.ROOT);
            data.set("kit-cooldown." + kit, null);
            plugin.users().save(target.getUniqueId());
            Text.ok(sender, "Reset <white>" + Text.escape(targetName) + "</white>'s cooldown for kit <white>"
                    + Text.escape(kit) + "</white>.");
        } else {
            data.set("kit-cooldown", null);
            plugin.users().save(target.getUniqueId());
            Text.ok(sender, "Reset all kit cooldowns for <white>" + Text.escape(targetName) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(plugin.kits().names(), args);
        }
        return List.of();
    }
}
