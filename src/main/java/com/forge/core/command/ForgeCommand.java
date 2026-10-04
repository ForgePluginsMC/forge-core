package com.forge.core.command;

import com.forge.core.ForgeCore;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Base class for every ForgeCore command.
 *
 * <p>Implementations are registered programmatically via
 * {@link CommandRegistry}; no plugin.yml entries are needed. Add each new
 * command to its pack's registrar (e.g. {@code TeleportPack.commands()}).
 *
 * <p>Rules for implementations:
 * <ul>
 *   <li>Never use deprecated Bukkit/Paper APIs.</li>
 *   <li>All user-visible text goes through {@code com.forge.core.util.Text}
 *       (MiniMessage). Escape untrusted input with {@code Text.escape(...)}.</li>
 *   <li>{@link #tabComplete} may run off the main thread — keep it free of
 *       world/chunk access.</li>
 *   <li>Permission defaults to {@code forgecore.<name>}; override only when a
 *       command genuinely needs a different node.</li>
 * </ul>
 */
public abstract class ForgeCommand {
    protected final ForgeCore plugin;

    protected ForgeCommand(ForgeCore plugin) {
        this.plugin = plugin;
    }

    /** Command name without slash, e.g. {@code "home"}. */
    public abstract String name();

    /** Aliases, e.g. {@code List.of("h", "homes")}. */
    public List<String> aliases() {
        return List.of();
    }

    /** One-line description shown in help. */
    public abstract String description();

    /** Permission node. Defaults to {@code forgecore.<name>}. */
    public String permission() {
        return "forgecore." + name();
    }

    /** Usage string, e.g. {@code "/home [player]"}. */
    public abstract String usage();

    /** When true, console/command blocks get a players-only error. */
    public boolean playerOnly() {
        return false;
    }

    /** Execute the command. Permission and player-only checks already passed. */
    public abstract void execute(CommandSender sender, String label, String[] args);

    /** Tab completions for the current (partial) argument list. */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    /** Cast sender to Player; null when the sender is not a player. */
    protected @Nullable Player asPlayer(CommandSender sender) {
        return sender instanceof Player player ? player : null;
    }
}
