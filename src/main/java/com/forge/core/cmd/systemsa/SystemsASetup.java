package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.alias.AliasManager;
import com.forge.core.cmd.systemsa.armorstand.ArmorStandEditor;
import com.forge.core.cmd.systemsa.attach.AttachManager;
import com.forge.core.cmd.systemsa.dsign.DynamicSignManager;
import com.forge.core.cmd.systemsa.hologram.HologramManager;
import com.forge.core.cmd.systemsa.ic.InteractiveManager;
import com.forge.core.cmd.systemsa.mirror.MirrorManager;
import com.forge.core.cmd.systemsa.portal.PortalManager;
import com.forge.core.cmd.systemsa.sc.SignCopyManager;

/**
 * Boots every Systems-A manager. Managers register their own listeners
 * and tasks in their constructors.
 */
public final class SystemsASetup {
    private static CustomRecipeGui customRecipes;

    private SystemsASetup() {
    }

    /** Initialize all Systems-A managers. Called once from the pack registrar. */
    public static void init(ForgeCore plugin) {
        new PortalManager(plugin);
        new HologramManager(plugin);
        new DynamicSignManager(plugin);
        new SignCopyManager(plugin);
        new MirrorManager(plugin);
        new ArmorStandEditor(plugin);
        new InteractiveManager(plugin);
        new AttachManager(plugin);
        new AliasManager(plugin);
        customRecipes = new CustomRecipeGui(plugin);
    }

    public static CustomRecipeGui customRecipes() {
        return customRecipes;
    }
}
