package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /giveall — give items to every online player. */
public final class GiveAllCommand extends ForgeCommand {
    public GiveAllCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "giveall";
    }

    @Override
    public String description() {
        return "Give items to all online players.";
    }

    @Override
    public String usage() {
        return "/giveall <item> [amount]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Material material = AdminUtil.material(args[0]);
        int amount = args.length >= 2 ? AdminUtil.intInRange(args[1], 1, 2304, "Amount") : 1;

        int count = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            GiveCommand.give(target, material, amount);
            Text.send(target, "You received <white>" + amount + " × "
                    + material.name().toLowerCase(Locale.ROOT) + "</white>.");
            count++;
        }
        Text.ok(sender, "Gave <white>" + amount + " × "
                + material.name().toLowerCase(Locale.ROOT) + "</white> to <white>"
                + count + "</white> players.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(AdminUtil.materialNames(), args);
        }
        return List.of();
    }
}
