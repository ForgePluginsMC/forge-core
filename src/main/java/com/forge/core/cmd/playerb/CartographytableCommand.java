package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import org.bukkit.inventory.MenuType;

/** /cartographytable — Open a cartography table anywhere. */
public final class CartographytableCommand extends StationCommand {
    public CartographytableCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "cartographytable";
    }

    @Override
    public String description() {
        return "Open a cartography table anywhere.";
    }

    @Override
    public String usage() {
        return "/cartographytable";
    }

    @Override
    protected MenuType.Typed<?, ?> menuType() {
        return MenuType.CARTOGRAPHY_TABLE;
    }

    @Override
    protected String title() {
        return "Cartography Table";
    }
}
