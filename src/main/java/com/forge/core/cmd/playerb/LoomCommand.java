package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import org.bukkit.inventory.MenuType;

/** /loom — Open a loom anywhere. */
public final class LoomCommand extends StationCommand {
    public LoomCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "loom";
    }

    @Override
    public String description() {
        return "Open a loom anywhere.";
    }

    @Override
    public String usage() {
        return "/loom";
    }

    @Override
    protected MenuType.Typed<?, ?> menuType() {
        return MenuType.LOOM;
    }

    @Override
    protected String title() {
        return "Loom";
    }
}
