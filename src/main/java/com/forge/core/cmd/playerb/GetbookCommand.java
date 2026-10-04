package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** /getbook — receive a book and quill. */
public final class GetbookCommand extends ForgeCommand {
    public GetbookCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "getbook";
    }

    @Override
    public String description() {
        return "Receive a book and quill.";
    }

    @Override
    public String usage() {
        return "/getbook";
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
        player.getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
        Text.ok(sender, "Here is your book and quill.");
    }
}
