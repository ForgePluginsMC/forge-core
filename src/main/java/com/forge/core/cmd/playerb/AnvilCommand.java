package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import org.bukkit.inventory.MenuType;

/** /anvil — Open an anvil anywhere. */
public final class AnvilCommand extends StationCommand {
    public AnvilCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "anvil";
    }

    @Override
    public String description() {
        return "Open an anvil anywhere.";
    }

    @Override
    public String usage() {
        return "/anvil";
    }

    @Override
    protected MenuType.Typed<?, ?> menuType() {
        return MenuType.ANVIL;
    }

    @Override
    protected String title() {
        return "Anvil";
    }
}
