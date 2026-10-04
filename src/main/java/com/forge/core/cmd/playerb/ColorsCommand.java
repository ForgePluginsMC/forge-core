package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;

/** /colors — show the MiniMessage color and format tags usable in chat. */
public final class ColorsCommand extends ForgeCommand {
    public ColorsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "colors";
    }

    @Override
    public String description() {
        return "Show the color and format tags you can use in chat.";
    }

    @Override
    public String usage() {
        return "/colors";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Text.send(sender, "<white>Colors:</white>");
        sender.sendMessage(Text.of(
                "<black>black</black> <dark_blue>dark_blue</dark_blue> <dark_green>dark_green</dark_green> "
                        + "<dark_aqua>dark_aqua</dark_aqua> <dark_red>dark_red</dark_red> "
                        + "<dark_purple>dark_purple</dark_purple> <gold>gold</gold> <gray>gray</gray> "
                        + "<dark_gray>dark_gray</dark_gray> <blue>blue</blue> <green>green</green> "
                        + "<aqua>aqua</aqua> <red>red</red> <light_purple>light_purple</light_purple> "
                        + "<yellow>yellow</yellow> <white>white</white>"));
        Text.send(sender, "<white>Formats:</white>");
        sender.sendMessage(Text.of(
                "<bold>bold</bold> <italic>italic</italic> <underlined>underlined</underlined> "
                        + "<strikethrough>strikethrough</strikethrough> <obfuscated>obfuscated</obfuscated> "
                        + "<reset>reset</reset>"));
        Text.send(sender, "<gray>Example: <white><red>Hello</red> <bold>world</bold></white></gray>");
    }
}
