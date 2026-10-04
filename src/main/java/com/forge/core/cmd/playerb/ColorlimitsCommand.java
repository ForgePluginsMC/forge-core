package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * /colorlimits — show which colors the sender may use, based on
 * {@code forgecore.color.<name>} permissions.
 */
public final class ColorlimitsCommand extends ForgeCommand {
    private static final List<String> COLOR_NAMES = List.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red",
            "light_purple", "yellow", "white");

    public ColorlimitsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "colorlimits";
    }

    @Override
    public String description() {
        return "Show which chat colors your rank may use.";
    }

    @Override
    public String usage() {
        return "/colorlimits";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        StringBuilder allowed = new StringBuilder();
        StringBuilder denied = new StringBuilder();
        for (String color : COLOR_NAMES) {
            if (sender.hasPermission("forgecore.color." + color)) {
                if (!allowed.isEmpty()) {
                    allowed.append(' ');
                }
                allowed.append('<').append(color).append('>').append(color).append("</").append(color).append('>');
            } else {
                if (!denied.isEmpty()) {
                    denied.append(' ');
                }
                denied.append("<dark_gray><strikethrough>").append(color).append("</strikethrough></dark_gray>");
            }
        }
        if (allowed.isEmpty()) {
            Text.send(sender, "You may not use any chat colors.");
        } else {
            sender.sendMessage(Text.of(Text.PREFIX + "<green>Allowed:</green> " + allowed));
        }
        if (!denied.isEmpty()) {
            sender.sendMessage(Text.of(Text.PREFIX + "<red>Denied:</red> " + denied));
        }
        Text.send(sender, "<gray>Grant with permission <white>forgecore.color.&lt;name&gt;</white>.</gray>");
    }
}
