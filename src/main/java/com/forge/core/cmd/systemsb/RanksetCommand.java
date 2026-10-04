package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.rank.RankManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/** Admin: set a player's rank directly. */
public final class RanksetCommand extends ForgeCommand {
    public RanksetCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "rankset";
    }

    @Override
    public String description() {
        return "Set a player's rank directly.";
    }

    @Override
    public String usage() {
        return "/rankset <player> <rank>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        OfflinePlayer target = Players.offline(args[0]);
        if (target == null) {
            throw new CommandRegistry.CommandFailure(
                    "Player " + Text.escape(args[0]) + " has never joined.");
        }
        RankManager.RankDef def = SystemsBSetup.ranks().byId(args[1]);
        if (def == null) {
            throw new CommandRegistry.CommandFailure("Unknown rank: " + Text.escape(args[1]) + ".");
        }
        SystemsBSetup.ranks().setRank(target, def);
        String name = target.getName() == null ? target.getUniqueId().toString() : target.getName();
        Text.ok(sender, "Set " + Text.escape(name) + "'s rank to " + def.display() + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            List<String> ids = new ArrayList<>();
            for (RankManager.RankDef def : SystemsBSetup.ranks().ladder()) {
                ids.add(def.id());
            }
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String id : ids) {
                if (id.startsWith(prefix)) {
                    matches.add(id);
                }
            }
            return matches;
        }
        return List.of();
    }
}
