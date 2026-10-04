package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** /preview — read-only look at another player's ender chest. */
public final class PreviewCommand extends ForgeCommand {
    public PreviewCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "preview";
    }

    @Override
    public String description() {
        return "Read-only preview of another player's ender chest.";
    }

    @Override
    public String usage() {
        return "/preview <player>";
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
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        Inventory view = Bukkit.createInventory(null, 27,
                Text.of("<gold>" + Text.escape(target.getName()) + "'s Ender Chest</gold> <gray>(read-only)</gray>"));
        view.setContents(target.getEnderChest().getContents());
        player.openInventory(view);
        Text.send(sender, "<gray>Previewing <white>" + Text.escape(target.getName())
                + "</white>'s ender chest — changes are not saved.</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
