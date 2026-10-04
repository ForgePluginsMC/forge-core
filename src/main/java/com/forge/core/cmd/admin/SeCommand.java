package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /se — set a line on the sign you are looking at. */
public final class SeCommand extends ForgeCommand {
    public SeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "se";
    }

    @Override
    public List<String> aliases() {
        return List.of("signedit");
    }

    @Override
    public String description() {
        return "Set a line on the sign you are looking at.";
    }

    @Override
    public String usage() {
        return "/se <line 1-4> <text...>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        int line = AdminUtil.intInRange(args[0], 1, 4, "Line");
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        Player player = (Player) sender;
        Block block = player.getTargetBlockExact(8);
        if (block == null || !(block.getState() instanceof Sign sign)) {
            Text.error(sender, "Look at a sign first.");
            return;
        }
        sign.getSide(Side.FRONT).line(line - 1, Component.text(text));
        sign.update();
        Text.ok(sender, "Sign line <white>" + line + "</white> set.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("1", "2", "3", "4"), args);
        }
        return List.of();
    }
}
