package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import org.bukkit.inventory.MenuType;

/** /grindstone — Open a grindstone anywhere. */
public final class GrindstoneCommand extends StationCommand {
    public GrindstoneCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "grindstone";
    }

    @Override
    public String description() {
        return "Open a grindstone anywhere.";
    }

    @Override
    public String usage() {
        return "/grindstone";
    }

    @Override
    protected MenuType.Typed<?, ?> menuType() {
        return MenuType.GRINDSTONE;
    }

    @Override
    protected String title() {
        return "Grindstone";
    }
}
