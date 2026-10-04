package com.forge.core.dialog;

import com.forge.core.ForgeCore;
import com.forge.core.dialog.DialogDef.DialogButton;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Manages dialog definitions and builds Paper dialogs on demand.
 *
 * <p>Dialogs persist in {@code dialogs.yml}. Buttons either run a command
 * (via command template) or open another dialog (via a linked show command).
 */
@NullMarked
public final class DialogManager {
    private final ForgeCore plugin;
    private final Map<String, DialogDef> dialogs = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public DialogManager(ForgeCore plugin) {
        this.plugin = plugin;
        load();
    }

    /** Get a dialog by id (case-insensitive). */
    public @Nullable DialogDef get(String id) {
        return dialogs.get(id.toLowerCase(Locale.ROOT));
    }

    /** All dialog ids. */
    public List<String> ids() {
        return new ArrayList<>(dialogs.keySet());
    }

    /** Create or replace a dialog. */
    public DialogDef create(String id, String title) {
        DialogDef def = new DialogDef(id.toLowerCase(Locale.ROOT), title);
        dialogs.put(def.id(), def);
        save();
        return def;
    }

    /** Remove a dialog. Returns true if it existed. */
    public boolean remove(String id) {
        boolean removed = dialogs.remove(id.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    /** Build a Paper dialog from a definition and show it to a player. */
    public boolean show(String id, Player player) {
        DialogDef def = get(id);
        if (def == null) {
            return false;
        }
        player.showDialog(build(def));
        return true;
    }

    private Dialog build(DialogDef def) {
        Component title = mm.deserialize(def.title());
        List<DialogBody> bodies = new ArrayList<>();
        if (!def.body().isEmpty()) {
            bodies.add(DialogBody.plainMessage(mm.deserialize(def.body())));
        }

        DialogBase base = DialogBase.builder(title)
                .body(bodies)
                .build();

        List<ActionButton> buttons = new ArrayList<>();
        for (DialogButton btn : def.buttons()) {
            buttons.add(buildButton(btn));
        }

        DialogType type;
        if (buttons.isEmpty()) {
            ActionButton close = ActionButton.builder(Component.text("Close")).build();
            type = DialogType.notice(close);
        } else if (buttons.size() == 2) {
            type = DialogType.confirmation(buttons.get(0), buttons.get(1));
        } else {
            ActionButton exit = ActionButton.builder(Component.text("Close")).build();
            type = DialogType.multiAction(buttons, exit, Math.min(buttons.size(), 3));
        }

        return Dialog.create(factory -> factory.empty().base(base).type(type));
    }

    private ActionButton buildButton(DialogButton btn) {
        Component label = mm.deserialize(btn.label());
        DialogAction action;
        if (btn.linkDialog() != null) {
            action = DialogAction.commandTemplate("/dialog show " + btn.linkDialog());
        } else if (btn.command() != null) {
            action = DialogAction.commandTemplate(btn.command());
        } else {
            action = DialogAction.commandTemplate("");
        }
        return ActionButton.builder(label).action(action).build();
    }

    /** Persist a change (called after mutations). */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (DialogDef def : dialogs.values()) {
            String path = "dialogs." + def.id();
            config.set(path + ".title", def.title());
            config.set(path + ".body", def.body());
            List<Map<String, String>> btnList = new ArrayList<>();
            for (DialogButton btn : def.buttons()) {
                Map<String, String> m = new HashMap<>();
                m.put("label", btn.label());
                if (btn.command() != null) {
                    m.put("command", btn.command());
                }
                if (btn.linkDialog() != null) {
                    m.put("link", btn.linkDialog());
                }
                btnList.add(m);
            }
            config.set(path + ".buttons", btnList);
        }
        try {
            config.save(plugin.getDataFolder().toPath().resolve("dialogs.yml").toFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save dialogs.yml: " + e.getMessage());
        }
    }

    private void load() {
        java.io.File file = plugin.getDataFolder().toPath().resolve("dialogs.yml").toFile();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("dialogs");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            String title = section.getString(id + ".title", id);
            String body = section.getString(id + ".body", "");
            DialogDef def = new DialogDef(id, title);
            def.body(body);
            List<Map<?, ?>> btnList = section.getMapList(id + ".buttons");
            for (Map<?, ?> m : btnList) {
                String label = String.valueOf(m.get("label"));
                Object cmdObj = m.get("command");
                Object linkObj = m.get("link");
                String command = cmdObj != null ? String.valueOf(cmdObj) : null;
                String link = linkObj != null ? String.valueOf(linkObj) : null;
                if (link != null) {
                    def.addButton(DialogButton.link(label, link));
                } else if (command != null) {
                    def.addButton(DialogButton.command(label, command));
                }
            }
            dialogs.put(id.toLowerCase(Locale.ROOT), def);
        }
    }
}
