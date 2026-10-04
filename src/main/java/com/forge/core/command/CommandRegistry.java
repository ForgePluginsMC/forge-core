package com.forge.core.command;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.admin.AdminPack;
import com.forge.core.cmd.economy.EconomyPack;
import com.forge.core.cmd.moderation.ModerationPack;
import com.forge.core.cmd.permission.PermissionPack;
import com.forge.core.cmd.playera.PlayerAPack;
import com.forge.core.cmd.playerb.PlayerBPack;
import com.forge.core.cmd.quest.QuestPack;
import com.forge.core.cmd.systemsa.SystemsAPack;
import com.forge.core.cmd.systemsb.SystemsBPack;
import com.forge.core.cmd.teleport.TeleportPack;
import com.forge.core.help.ForgeCoreCommand;
import com.forge.core.merge.announcer.AnnouncerPack;
import com.forge.core.merge.chat.ChatPack;
import com.forge.core.merge.items.ItemsPack;
import com.forge.core.merge.playtime.PlaytimePack;
import com.forge.core.merge.stack.StackPack;
import com.forge.core.util.Text;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Registers every {@link ForgeCommand} through Paper's command lifecycle —
 * no plugin.yml command section required.
 */
public final class CommandRegistry {
    private static int count;
    private static List<ForgeCommand> registered = List.of();

    private CommandRegistry() {
    }

    /** Number of commands registered on the last enable. */
    public static int count() {
        return count;
    }

    /** Every command registered on the last enable (for help, etc.). */
    public static List<ForgeCommand> commands() {
        return registered;
    }

    /** Collect every command from every pack. */
    public static List<ForgeCommand> all(ForgeCore plugin) {
        List<ForgeCommand> commands = new ArrayList<>();
        commands.addAll(TeleportPack.commands(plugin));
        commands.addAll(ModerationPack.commands(plugin));
        commands.addAll(EconomyPack.commands(plugin));
        commands.addAll(PlayerAPack.commands(plugin));
        commands.addAll(PlayerBPack.commands(plugin));
        commands.addAll(AdminPack.commands(plugin));
        commands.addAll(SystemsAPack.commands(plugin));
        commands.addAll(SystemsBPack.commands(plugin));
        commands.add(new ForgeCoreCommand(plugin));
        commands.addAll(PlaytimePack.commands(plugin));
        commands.addAll(AnnouncerPack.commands(plugin));
        commands.addAll(ChatPack.commands(plugin));
        commands.addAll(StackPack.commands(plugin));
        commands.addAll(ItemsPack.commands(plugin));
        commands.addAll(PermissionPack.commands(plugin));
        commands.addAll(com.forge.core.guild.GuildPack.commands(plugin));
        commands.addAll(QuestPack.commands(plugin));
        commands.addAll(com.forge.core.cmd.dialog.DialogPack.commands(plugin));
        commands.add(new com.forge.core.gui.MenuCommand(plugin));
        commands.addAll(com.forge.core.gui.dashboard.DashboardPack.commands(plugin));
        return commands;
    }

    /** Register every command with Paper. Called once from {@code onEnable}. */
    public static void registerAll(ForgeCore plugin) {
        List<ForgeCommand> commands = all(plugin);
        count = commands.size();
        registered = List.copyOf(commands);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            for (ForgeCommand command : commands) {
                registrar.register(
                        plugin.getPluginMeta(),
                        command.name(),
                        command.description(),
                        command.aliases(),
                        new Adapter(command));
            }
        });
    }

    /** Bridges {@link ForgeCommand} to Paper's {@link BasicCommand}. */
    private static final class Adapter implements BasicCommand {
        private final ForgeCommand command;

        Adapter(ForgeCommand command) {
            this.command = command;
        }

        @Override
        public void execute(CommandSourceStack stack, String[] args) {
            CommandSender sender = stack.getSender();
            if (command.playerOnly() && !(sender instanceof Player)) {
                Text.error(sender, "Only players can use that command.");
                return;
            }
            String permission = command.permission();
            if (!permission.isEmpty() && !command.plugin.permissions().hasPermission(sender, permission)) {
                Text.error(sender, "You don't have permission to do that.");
                return;
            }
            try {
                command.execute(sender, command.name(), args);
            } catch (CommandFailure failure) {
                Text.error(sender, failure.getMessage());
                String usage = failure.usageOverride() != null ? failure.usageOverride() : command.usage();
                Text.usage(sender, usage);
            } catch (Exception exception) {
                Text.error(sender, "Something went wrong running that command.");
                command.plugin.getLogger().warning(
                        "Command /" + command.name() + " failed: " + exception);
            }
        }

        @Override
        public Collection<String> suggest(CommandSourceStack stack, String[] args) {
            String permission = command.permission();
            if (!permission.isEmpty() && !command.plugin.permissions().hasPermission(stack.getSender(), permission)) {
                return List.of();
            }
            try {
                return command.tabComplete(stack.getSender(), args);
            } catch (Exception exception) {
                return List.of();
            }
        }

        @Override
        public @Nullable String permission() {
            // Handled manually in execute() so messages stay consistent.
            return null;
        }
    }

    /**
     * Throw from {@link ForgeCommand#execute} to abort with a clean
     * user-facing error message. The command's usage hint is shown
     * automatically after the message; pass {@code usageOverride} for a
     * more specific usage line (e.g. a subcommand's syntax).
     */
    public static final class CommandFailure extends RuntimeException {
        private final @Nullable String usageOverride;

        public CommandFailure(String message) {
            this(message, null);
        }

        public CommandFailure(String message, @Nullable String usageOverride) {
            super(message);
            this.usageOverride = usageOverride;
        }

        /** Specific usage line to show instead of the command's {@code usage()}. */
        public @Nullable String usageOverride() {
            return usageOverride;
        }
    }
}
