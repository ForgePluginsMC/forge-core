package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.playerb.MailManager;
import com.forge.core.cmd.playerb.PlayerBState;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Mail inbox: click a message to read it, buttons to clear and compose.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class MailGui extends GlyphGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public MailGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE114";
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Mail");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        MailManager mail = PlayerBState.mail();
        List<MailManager.Mail> messages = mail.read(viewer.getUniqueId());

        int[] slots = CONTENT_SLOTS;
        for (int i = 0; i < messages.size() && i < slots.length; i++) {
            MailManager.Mail msg = messages.get(i);
            String preview = msg.message().length() > 30
                    ? msg.message().substring(0, 30) + "..."
                    : msg.message();
            set(slots[i], tile(ForgeIcons.TILE_YELLOW,
                    "<yellow><bold>From: <white>" + Text.escape(msg.from()),
                    List.of("<gray>" + Text.escape(preview),
                            "", "<green>Click to read full message"),
                    p -> {
                        p.closeInventory();
                        Text.send(p, "<yellow><bold>From " + Text.escape(msg.from()) + ":");
                        Text.send(p, "<white>" + Text.escape(msg.message()));
                    }));
        }

        if (messages.isEmpty()) {
            set(22, tile(ForgeIcons.TILE_GRAY, "<gray>Inbox empty",
                    List.of("<dark_gray>No messages."), p -> {}));
        }
    }

    @Override
    protected void buildFooter(Player viewer) {
        super.buildFooter(viewer);
        set(47, tile(ForgeIcons.TILE_GREEN, "<green><bold>Compose",
                List.of("<gray>Send mail to a player.",
                        "", "<yellow>Click to enter recipient"),
                p -> {
                    p.closeInventory();
                    Text.send(p, "<yellow>Type the recipient's name (or 'cancel'):");
                    ChatInput.request(p, recipient -> {
                        if (recipient.equalsIgnoreCase("cancel")) {
                            Text.send(p, "<gray>Cancelled.");
                            return;
                        }
                        Text.send(p, "<yellow>Type your message (or 'cancel'):");
                        ChatInput.request(p, message -> {
                            if (message.equalsIgnoreCase("cancel")) {
                                Text.send(p, "<gray>Cancelled.");
                                return;
                            }
                            p.performCommand("mail " + recipient + " " + message);
                        });
                    });
                }));
        set(51, tile(ForgeIcons.TILE_RED, "<red><bold>Clear",
                List.of("<gray>Delete all messages.",
                        "", "<red>Click to clear"),
                p -> runCommand(p, "mail clear")));
    }
}
