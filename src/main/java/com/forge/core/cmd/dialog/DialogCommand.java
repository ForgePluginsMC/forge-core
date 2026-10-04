package com.forge.core.cmd.dialog;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.dialog.DialogDef;
import com.forge.core.dialog.DialogManager;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * /dialog — build and show Paper Dialog API dialogs.
 *
 * <p>Subcommands: create, body, button, link, show, list, remove.
 */
@NullMarked
public final class DialogCommand extends ForgeCommand {
    public DialogCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dialog";
    }

    @Override
    public String description() {
        return "Create and show fullscreen dialogs.";
    }

    @Override
    public String usage() {
        return "/dialog <create|body|button|link|show|list|remove> ...";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        DialogManager dialogs = plugin.dialogs();
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> {
                if (args.length < 3) {
                    Text.usage(sender, "/dialog create <id> <title...>");
                    return;
                }
                String id = args[1].toLowerCase(Locale.ROOT);
                String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                dialogs.create(id, title);
                Text.ok(sender, "Dialog '" + id + "' created.");
            }
            case "body" -> {
                if (args.length < 3) {
                    Text.usage(sender, "/dialog body <id> <text...>");
                    return;
                }
                DialogDef def = dialogs.get(args[1].toLowerCase(Locale.ROOT));
                if (def == null) {
                    Text.error(sender, "No dialog with id '" + args[1] + "'.");
                    return;
                }
                def.body(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                Text.ok(sender, "Body set for '" + def.id() + "'.");
            }
            case "button" -> {
                if (args.length < 4) {
                    Text.usage(sender, "/dialog button <id> <label> <command...>");
                    return;
                }
                DialogDef def = dialogs.get(args[1].toLowerCase(Locale.ROOT));
                if (def == null) {
                    Text.error(sender, "No dialog with id '" + args[1] + "'.");
                    return;
                }
                String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                def.addButton(DialogDef.DialogButton.command(args[2], command));
                Text.ok(sender, "Button '" + args[2] + "' added to '" + def.id() + "'.");
            }
            case "link" -> {
                if (args.length < 4) {
                    Text.usage(sender, "/dialog link <id> <label> <target-dialog>");
                    return;
                }
                DialogDef def = dialogs.get(args[1].toLowerCase(Locale.ROOT));
                if (def == null) {
                    Text.error(sender, "No dialog with id '" + args[1] + "'.");
                    return;
                }
                def.addButton(DialogDef.DialogButton.link(args[2], args[3].toLowerCase(Locale.ROOT)));
                Text.ok(sender, "Link button '" + args[2] + "' added to '" + def.id() + "'.");
            }
            case "show" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/dialog show <id> [player]");
                    return;
                }
                Player target;
                if (args.length > 2) {
                    target = plugin.getServer().getPlayer(args[2]);
                    if (target == null) {
                        Text.error(sender, "Player not found: " + args[2]);
                        return;
                    }
                } else {
                    if (!(sender instanceof Player p)) {
                        Text.error(sender, "Specify a player when running from console.");
                        return;
                    }
                    target = p;
                }
                if (!dialogs.show(args[1].toLowerCase(Locale.ROOT), target)) {
                    Text.error(sender, "No dialog with id '" + args[1] + "'.");
                    return;
                }
                Text.ok(sender, "Dialog shown to " + target.getName() + ".");
            }
            case "list" -> {
                List<String> ids = dialogs.ids();
                if (ids.isEmpty()) {
                    Text.send(sender, "No dialogs defined.");
                    return;
                }
                Text.send(sender, "Dialogs: " + String.join(", ", ids));
            }
            case "remove" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/dialog remove <id>");
                    return;
                }
                if (!dialogs.remove(args[1].toLowerCase(Locale.ROOT))) {
                    Text.error(sender, "No dialog with id '" + args[1] + "'.");
                    return;
                }
                Text.ok(sender, "Dialog '" + args[1] + "' removed.");
            }
            default -> Text.usage(sender, usage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String s : List.of("create", "body", "button", "link", "show", "list", "remove")) {
                if (s.startsWith(prefix)) {
                    out.add(s);
                }
            }
        } else if (args.length == 2
                && !args[0].equalsIgnoreCase("create")
                && !args[0].equalsIgnoreCase("list")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (String id : plugin.dialogs().ids()) {
                if (id.startsWith(prefix)) {
                    out.add(id);
                }
            }
        }
        return out;
    }
}
