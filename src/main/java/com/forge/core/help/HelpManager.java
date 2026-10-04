package com.forge.core.help;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;

/**
 * Renders ForgeCore's clickable paginated help.
 *
 * <p>Entry points: {@code /help forgecore [page|command]} (intercepted from
 * vanilla help by {@link HelpListener}) and {@code /forgecore help
 * [page|command]} (real command, also usable from console).
 */
public final class HelpManager {
    private static final int PAGE_SIZE = 10;

    private final ForgeCore plugin;

    public HelpManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new HelpListener(this), plugin);
        plugin.getServer().getHelpMap().addTopic(new ForgeCoreHelpTopic());
    }

    /**
     * Show help. No args: page 1. Numeric arg: that page. Anything else:
     * detail for the named command.
     */
    public void showHelp(CommandSender sender, String[] args) {
        if (args.length >= 1 && !isNumeric(args[0])) {
            showDetail(sender, args[0]);
            return;
        }
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }
        showPage(sender, page);
    }

    private void showPage(CommandSender sender, int page) {
        List<ForgeCommand> commands = visibleCommands(sender);
        int pages = Math.max(1, (commands.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.min(Math.max(1, page), pages);

        sender.sendMessage(Text.of("<gold><bold>ForgeCore</bold></gold> <gray>commands — page <white>" + page
                + "</white> of <white>" + pages + "</white> <dark_gray>(</dark_gray><white>" + commands.size()
                + "</white><dark_gray> you can use)</dark_gray>"));
        int from = (page - 1) * PAGE_SIZE;
        for (int i = from; i < Math.min(from + PAGE_SIZE, commands.size()); i++) {
            ForgeCommand command = commands.get(i);
            String hover = Text.escape(command.description()).replace("'", "");
            sender.sendMessage(Text.of("  <click:run_command:'/help forgecore " + command.name() + "'>"
                    + "<hover:show_text:'" + hover + "'>"
                    + "<yellow>/" + command.name() + "</yellow></hover></click>"
                    + " <gray>— " + Text.escape(command.description()) + "</gray>"));
        }
        String prev = page > 1
                ? "<click:run_command:'/help forgecore " + (page - 1) + "'><yellow>« Prev</yellow></click>"
                : "<gray>« Prev</gray>";
        String next = page < pages
                ? "<click:run_command:'/help forgecore " + (page + 1) + "'><yellow>Next »</yellow></click>"
                : "<gray>Next »</gray>";
        sender.sendMessage(Text.of(prev + " <dark_gray>|</dark_gray> <gray>page <white>" + page + "</white> of <white>"
                + pages + "</white></gray> <dark_gray>|</dark_gray> " + next));
    }

    private void showDetail(CommandSender sender, String name) {
        ForgeCommand target = null;
        String lowered = name.toLowerCase(Locale.ROOT);
        for (ForgeCommand command : CommandRegistry.commands()) {
            if (command.name().equalsIgnoreCase(lowered)
                    || command.aliases().stream().anyMatch(a -> a.equalsIgnoreCase(lowered))) {
                target = command;
                break;
            }
        }
        if (target == null) {
            Text.error(sender, "Unknown ForgeCore command: <white>" + Text.escape(name) + "</white>.");
            return;
        }
        if (!target.permission().isEmpty() && !sender.hasPermission(target.permission())) {
            Text.error(sender, "You don't have permission to view that command.");
            return;
        }
        sender.sendMessage(Text.of("<gold><bold>/" + target.name() + "</bold></gold> <gray>— "
                + Text.escape(target.description()) + "</gray>"));
        Text.usage(sender, target.usage());
        if (!target.aliases().isEmpty()) {
            sender.sendMessage(Text.of("<gray>Aliases: </gray><white>/"
                    + String.join(", /", target.aliases()) + "</white>"));
        }
        if (!target.permission().isEmpty()) {
            sender.sendMessage(Text.of("<gray>Permission: </gray><white>"
                    + Text.escape(target.permission()) + "</white>"));
        }
    }

    private List<ForgeCommand> visibleCommands(CommandSender sender) {
        return CommandRegistry.commands().stream()
                .filter(command -> command.permission().isEmpty() || sender.hasPermission(command.permission()))
                .sorted(Comparator.comparing(ForgeCommand::name))
                .toList();
    }

    private static boolean isNumeric(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** HelpMap topic so console {@code /help forgecore} degrades gracefully. */
    private static final class ForgeCoreHelpTopic extends HelpTopic {
        ForgeCoreHelpTopic() {
            name = "forgecore";
            shortText = "ForgeCore command help";
            fullText = "ForgeCore's clickable help needs a player. From console use /forgecore help [page|command].";
        }

        @Override
        public boolean canSee(CommandSender sender) {
            return true;
        }
    }
}
