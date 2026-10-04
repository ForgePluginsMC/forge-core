package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.Merchant;

/**
 * /merchant — open a villager trading GUI (empty trade list).
 *
 * <p>Uses Paper's Menu Type API ({@link MenuType#MERCHANT}) — the non-deprecated
 * replacement for the old {@code HumanEntity#openMerchant} methods.
 */
public final class MerchantCommand extends ForgeCommand {
    public MerchantCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "merchant";
    }

    @Override
    public String description() {
        return "Open a villager merchant trading GUI.";
    }

    @Override
    public String usage() {
        return "/merchant";
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
        Merchant merchant = Bukkit.createMerchant();
        MenuType.MERCHANT.builder()
                .merchant(merchant)
                .title(Component.text("Merchant"))
                .build(player)
                .open();
        Text.ok(sender, "Merchant opened.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
