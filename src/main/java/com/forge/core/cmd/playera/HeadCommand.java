package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/** Give yourself (or a target) a player head. */
public final class HeadCommand extends PlayerACommand {
    public HeadCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "head";
    }

    @Override
    public String description() {
        return "Get a player head.";
    }

    @Override
    public String usage() {
        return "/head [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (!(skull.getItemMeta() instanceof SkullMeta meta)) {
            Text.error(sender, "Could not create a player head.");
            return;
        }
        meta.setPlayerProfile(Bukkit.createProfile(target.getUniqueId(), target.getName()));
        meta.displayName(Text.of("<white>" + Text.escape(target.getName()) + "'s Head</white>"));
        skull.setItemMeta(meta);
        var leftover = player.getInventory().addItem(skull);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
        Text.ok(sender, "Gave you <white>" + Text.escape(target.getName()) + "'s</white> head.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
