package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import org.bukkit.inventory.MenuType;

/** /smithingtable — Open a smithing table anywhere. */
public final class SmithingtableCommand extends StationCommand {
    public SmithingtableCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "smithingtable";
    }

    @Override
    public String description() {
        return "Open a smithing table anywhere.";
    }

    @Override
    public String usage() {
        return "/smithingtable";
    }

    @Override
    protected MenuType.Typed<?, ?> menuType() {
        return MenuType.SMITHING;
    }

    @Override
    protected String title() {
        return "Smithing Table";
    }
}
