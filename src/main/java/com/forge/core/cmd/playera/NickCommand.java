package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Set a MiniMessage nickname (or {@code off} to clear). Colors need
 * {@code forgecore.nick.color}; targeting others needs
 * {@code forgecore.nick.others}.
 */
public final class NickCommand extends PlayerACommand {
    public NickCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "nick";
    }

    @Override
    public String description() {
        return "Set a nickname (off to clear).";
    }

    @Override
    public String usage() {
        return "/nick [player] <nick|off>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player self = (Player) sender;
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player target;
        String nickArg;
        if (args.length == 1) {
            target = self;
            nickArg = args[0];
        } else {
            if (!sender.hasPermission("forgecore.nick.others")) {
                Text.error(sender, "You don't have permission to use that on other players.");
                return;
            }
            target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
            nickArg = args[1];
        }
        UserData data = plugin.users().get(target);
        if (nickArg.equalsIgnoreCase("off")) {
            data.setNick(null);
            Nicks.applyRaw(plugin, target, null);
            plugin.users().save(target.getUniqueId());
            Text.ok(sender, "Nickname cleared for <white>" + Text.escape(target.getName()) + "</white>.");
            return;
        }
        String mini = sender.hasPermission("forgecore.nick.color") ? nickArg : Text.strip(nickArg);
        if (Text.strip(mini).isBlank()) {
            Text.error(sender, "Nickname cannot be blank.");
            return;
        }
        data.setNick(mini);
        Nicks.applyRaw(plugin, target, mini);
        plugin.users().save(target.getUniqueId());
        Text.ok(sender, "Nickname set to " + mini + "<green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
