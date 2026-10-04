package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;

/**
 * /editctext — manage custom texts: create, delete, set, list, info.
 * Text supports MiniMessage markup and placeholders (see /placeholders).
 */
public final class EditctextCommand extends ForgeCommand {
    private static final List<String> SUBS = List.of("create", "delete", "set", "list", "info");

    public EditctextCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "editctext";
    }

    @Override
    public String description() {
        return "Create, edit and delete custom texts for /ctext.";
    }

    @Override
    public String usage() {
        return "/editctext <create|delete|set|list|info> <name> [text...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        CTextManager manager = CTextManager.get();
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> {
                List<String> names = manager.names();
                if (names.isEmpty()) {
                    Text.send(sender, "No custom texts yet. Create one with <white>/editctext create</white>.");
                } else {
                    Text.send(sender, "Custom texts (<white>" + names.size() + "</white>): <white>"
                            + Text.escape(String.join(", ", names)) + "</white>.");
                }
            }
            case "info" -> {
                if (args.length != 2) {
                    Text.usage(sender, "/editctext info <name>");
                    return;
                }
                CTextManager.CText text = manager.get(args[1]);
                if (text == null) {
                    Text.error(sender, "No custom text named <white>" + Text.escape(args[1]) + "</white>.");
                    return;
                }
                Text.send(sender, "<white>" + Text.escape(text.name()) + "</white> by <white>"
                        + Text.escape(text.creator()) + "</white> <gray>("
                        + Time.formatDate(text.created()) + ")</gray>");
                sender.sendMessage(Text.of(text.text()));
            }
            case "create", "set" -> {
                if (args.length < 3) {
                    Text.usage(sender, "/editctext " + args[0].toLowerCase(Locale.ROOT) + " <name> <text...>");
                    return;
                }
                boolean existed = manager.exists(args[1]);
                if (args[0].equalsIgnoreCase("create") && existed) {
                    Text.error(sender, "That name exists. Use <white>/editctext set</white> to overwrite it.");
                    return;
                }
                String text = String.join(" ", argsRange(args, 2));
                manager.set(args[1], text, sender.getName());
                Text.ok(sender, (existed ? "Updated" : "Created") + " custom text <white>"
                        + Text.escape(args[1]) + "</white>.");
            }
            case "delete", "remove" -> {
                if (args.length != 2) {
                    Text.usage(sender, "/editctext delete <name>");
                    return;
                }
                if (manager.delete(args[1])) {
                    Text.ok(sender, "Deleted custom text <white>" + Text.escape(args[1]) + "</white>.");
                } else {
                    Text.error(sender, "No custom text named <white>" + Text.escape(args[1]) + "</white>.");
                }
            }
            default -> Text.usage(sender, usage());
        }
    }

    private static String[] argsRange(String[] args, int from) {
        String[] out = new String[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(SUBS, args);
        }
        if (args.length == 2
                && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("set")
                        || args[0].equalsIgnoreCase("info"))) {
            return Players.filter(CTextManager.get().names(), args);
        }
        return List.of();
    }
}
