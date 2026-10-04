package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Systems-A pack registrar: interactive world systems.
 *
 * <p>Commands in this pack: portals, hologram, dsign, sc, mirror,
 * armorstand, ic, attachcommand, aliaseditor.
 *
 * <p>Each system owns its manager under
 * {@code com.forge.core.cmd.systemsa.<system>}. Managers register their own
 * listeners and tasks in their constructors (booted via
 * {@link SystemsASetup#init(ForgeCore)}).
 */
public final class SystemsAPack {
    private SystemsAPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        SystemsASetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new PortalsCommand(plugin));
        commands.add(new HologramCommand(plugin));
        commands.add(new DsignCommand(plugin));
        commands.add(new ScCommand(plugin));
        commands.add(new MirrorCommand(plugin));
        commands.add(new ArmorstandCommand(plugin));
        commands.add(new IcCommand(plugin));
        commands.add(new AttachcommandCommand(plugin));
        commands.add(new AliaseditorCommand(plugin));
        commands.add(new CustomrecipeCommand(plugin));
        return commands;
    }
}
