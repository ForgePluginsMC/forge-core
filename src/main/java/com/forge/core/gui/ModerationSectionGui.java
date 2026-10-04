package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Moderation section: punishments, player info, management.
 *
 * <p>Glyph-rendered: button art is baked into the title glyph; slots hold
 * invisible click targets with tooltips.
 */
@NullMarked
public final class ModerationSectionGui extends GlyphGui {
    /** PUA glyphs for moderation pages 1-3. */
    private static final String[] GLYPHS = {"\uE10C", "\uE10D", "\uE10E"};

    private final ForgeCore plugin;
    private final WebGui parent;
    private final int page;

    public ModerationSectionGui(ForgeCore plugin, WebGui parent) {
        this(plugin, parent, 0);
    }

    public ModerationSectionGui(ForgeCore plugin, WebGui parent, int page) {
        this.plugin = plugin;
        this.parent = parent;
        this.page = Math.max(0, page);
    }

    @Override
    protected String glyphChar() {
        return GLYPHS[Math.min(page, GLYPHS.length - 1)];
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Moderation");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    /** A section button: short baked label, tooltip description, click action. */
    private record SecButton(String label, String desc, Consumer<Player> action) {
    }

    private List<SecButton> buttons() {
        List<SecButton> out = new ArrayList<>();
        out.add(new SecButton("BAN", "Ban a player from the server",
                p -> inputCmd(p, "Player to ban:", "ban")));
        out.add(new SecButton("TEMPBAN", "Temporarily ban a player",
                p -> inputCmd(p, "Player and duration (e.g. Steve 1d):", "tempban")));
        out.add(new SecButton("KICK", "Kick a player",
                p -> inputCmd(p, "Player to kick:", "kick")));
        out.add(new SecButton("MUTE", "Mute a player's chat",
                p -> inputCmd(p, "Player to mute:", "mute")));
        out.add(new SecButton("WARN", "Issue a warning",
                p -> inputCmd(p, "Player and reason:", "warn")));
        out.add(new SecButton("BAN IP", "Ban an IP address",
                p -> inputCmd(p, "IP or player:", "banip")));
        out.add(new SecButton("JAIL", "Jail a player",
                p -> inputCmd(p, "Player to jail:", "jail")));
        out.add(new SecButton("PLAYERS", "View and manage players",
                p -> new PlayerListGui(plugin, this).open(p)));
        out.add(new SecButton("INFO", "Check player details",
                p -> inputCmd(p, "Player name:", "seen")));
        out.add(new SecButton("CHECK BAN", "Check ban status",
                p -> inputCmd(p, "Player name:", "checkban")));
        out.add(new SecButton("BAN LIST", "View and manage bans",
                p -> new BanListGui(plugin, this).open(p)));
        out.add(new SecButton("UNBAN", "Unban a player",
                p -> inputCmd(p, "Player to unban:", "unban")));
        out.add(new SecButton("UNMUTE", "Unmute a player",
                p -> inputCmd(p, "Player to unmute:", "unmute")));
        out.add(new SecButton("VANISH", "Toggle invisibility",
                p -> runCmd(p, "vanish")));
        out.add(new SecButton("SPY", "Monitor private messages",
                p -> runCmd(p, "socialspy")));
        out.add(new SecButton("CLEARCHAT", "Clear public chat",
                p -> runCmd(p, "clearchat")));
        return out;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<SecButton> all = buttons();
        int start = page * 6;
        for (int i = 0; i < 6 && start + i < all.size(); i++) {
            SecButton b = all.get(start + i);
            GuiItem item = ghostTip("<red><bold>" + b.label(),
                    List.of("<gray>" + b.desc()), b.action());
            for (int slot : CARD_SLOTS[i]) {
                set(slot, item);
            }
        }
    }

    @Override
    protected void buildFooter(Player viewer) {
        super.buildFooter(viewer);
        int totalPages = (buttons().size() + 5) / 6;
        if (page > 0) {
            set(SLOT_PREV, ghost(p -> new ModerationSectionGui(plugin, parent, page - 1).open(p)));
        }
        if (page < totalPages - 1) {
            set(SLOT_NEXT, ghost(p -> new ModerationSectionGui(plugin, parent, page + 1).open(p)));
        }
    }

    private void runCmd(Player p, String cmd) {
        p.closeInventory();
        p.performCommand(cmd);
    }

    private void inputCmd(Player p, String prompt, String cmd) {
        p.closeInventory();
        com.forge.core.util.Text.send(p, "<yellow>" + prompt + " <gray>(type in chat, or 'cancel')");
        ChatInput.request(p, input -> {
            if (input.equalsIgnoreCase("cancel")) {
                com.forge.core.util.Text.send(p, "<gray>Cancelled.");
                return;
            }
            p.performCommand(cmd + " " + input);
        });
    }
}
