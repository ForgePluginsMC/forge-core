package com.forge.core.merge.items;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.List;

/** Command registrar for the merged forge-items system. */
public final class ItemsPack {
    private ItemsPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        ItemsSetup.init(plugin);
        return List.of(new FitemsCommand(plugin));
    }
}
