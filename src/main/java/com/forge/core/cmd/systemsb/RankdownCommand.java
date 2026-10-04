package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.rank.RankManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Drop down one rank on the ladder. */
public final class RankdownCommand extends ForgeCommand {
    public RankdownCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "rankdown";
    }

    @Override
    public String description() {
        return "Drop down one rank on the ladder.";
    }

    @Override
    public String usage() {
        return "/rankdown";
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
        RankManager ranks = SystemsBSetup.ranks();
        RankManager.RankDef prev = ranks.prevOf(player);
        if (prev == null) {
            throw new CommandRegistry.CommandFailure("You are already at the lowest rank.");
        }
        ranks.demote(player, prev);
        Text.ok(sender, "Demoted to " + prev.display() + ".");
    }
}
