package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import org.bukkit.inventory.MenuType;

/** /stonecutter — Open a stonecutter anywhere. */
public final class StonecutterCommand extends StationCommand {
    public StonecutterCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "stonecutter";
    }

    @Override
    public String description() {
        return "Open a stonecutter anywhere.";
    }

    @Override
    public String usage() {
        return "/stonecutter";
    }

    @Override
    protected MenuType.Typed<?, ?> menuType() {
        return MenuType.STONECUTTER;
    }

    @Override
    protected String title() {
        return "Stonecutter";
    }
}
