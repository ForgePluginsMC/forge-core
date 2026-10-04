package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Base for player-a commands. Every command in this pack is player-only; the
 * registry adapter enforces that before {@link #execute} runs, so
 * {@code (Player) sender} is always safe here.
 */
abstract class PlayerACommand extends ForgeCommand {
    PlayerACommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public final boolean playerOnly() {
        return true;
    }

    /**
     * Optional target from {@code args[0]} (requires
     * {@code <permission>.others}); the sender when absent. Returns null when
     * the lookup failed — the failure has already been messaged.
     */
    protected @Nullable Player target(CommandSender sender, String[] args) {
        Player self = (Player) sender;
        if (args.length == 0) {
            return self;
        }
        if (!sender.hasPermission(permission() + ".others")) {
            Text.error(sender, "You don't have permission to use that on other players.");
            return null;
        }
        return Players.find(sender, args[0]);
    }

    /**
     * Required target from {@code args[0]}. Returns null when the argument is
     * missing (usage shown) or the lookup failed (already messaged).
     */
    protected @Nullable Player requiredTarget(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return null;
        }
        return Players.find(sender, args[0]);
    }

    /**
     * Target from {@code args[index]} with {@code .others} permission check;
     * the sender when no such argument exists. Returns null on failure
     * (already messaged).
     */
    protected @Nullable Player targetAt(CommandSender sender, String[] args, int index) {
        Player self = (Player) sender;
        if (args.length <= index) {
            return self;
        }
        if (!sender.hasPermission(permission() + ".others")) {
            Text.error(sender, "You don't have permission to use that on other players.");
            return null;
        }
        return Players.find(sender, args[index]);
    }

    /** Online player names filtered by the last argument; for tab completion. */
    protected List<String> playerNames(String[] args) {
        return Players.filter(Players.onlineNames(), args);
    }
}
