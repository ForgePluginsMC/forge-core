package com.forge.core.merge.items;

import com.forge.core.ForgeCore;

/**
 * Lifecycle wiring for the merged forge-items system. Idempotent: safe to
 * call from both the pack registrar and the parent's enable path.
 */
public final class ItemsSetup {
    private static boolean done;
    private static ItemsModule module;

    private ItemsSetup() {
    }

    /** Builds the module, loads data, registers listeners and starts tasks. */
    public static synchronized void init(ForgeCore plugin) {
        if (done) {
            return;
        }
        done = true;
        module = new ItemsModule(plugin);
        module.reloadItems();
        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(module.listener(), plugin);
        pm.registerEvents(module.setBonuses(), plugin);
        pm.registerEvents(module.drops(), plugin);
        pm.registerEvents(module.browser(), plugin);
        pm.registerEvents(module.chatInput(), plugin);
        pm.registerEvents(module.editor(), plugin);
        module.listener().startLoopTask();
        module.setBonuses().start();
        module.mana().start();
        module.chatInput().start();
        plugin.getLogger().info("ForgeCore items: " + module.itemCount() + " item(s), "
                + module.setCount() + " set(s), " + module.dropCount() + " mob drop(s).");
    }

    /** The live module; non-null after {@link #init}. */
    public static ItemsModule module() {
        return module;
    }

    /**
     * Shutdown hook. Item data is written explicitly by the editor and
     * commands, so there is nothing pending to flush; tasks are owned by
     * ForgeCore and die with it.
     */
    public static void shutdown(ForgeCore plugin) {
        // No-op by design; see javadoc.
    }
}
