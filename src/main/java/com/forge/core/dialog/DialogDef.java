package com.forge.core.dialog;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Definition of a dialog: title, body text, and buttons.
 *
 * <p>Built into a Paper {@link io.papermc.paper.dialog.Dialog} on demand by
 * {@link DialogManager}.
 */
@NullMarked
public final class DialogDef {
    private final String id;
    private String title;
    private String body;
    private final List<DialogButton> buttons = new ArrayList<>();

    public DialogDef(String id, String title) {
        this.id = id;
        this.title = title;
        this.body = "";
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }

    public String body() {
        return body;
    }

    public void body(String body) {
        this.body = body;
    }

    public List<DialogButton> buttons() {
        return buttons;
    }

    public void addButton(DialogButton button) {
        buttons.add(button);
    }

    /** A button: label plus an action (command or link to another dialog). */
    @NullMarked
    public static final class DialogButton {
        private final String label;
        private final @Nullable String command;
        private final @Nullable String linkDialog;

        private DialogButton(String label, @Nullable String command, @Nullable String linkDialog) {
            this.label = label;
            this.command = command;
            this.linkDialog = linkDialog;
        }

        public static DialogButton command(String label, String command) {
            return new DialogButton(label, command, null);
        }

        public static DialogButton link(String label, String dialogId) {
            return new DialogButton(label, null, dialogId);
        }

        public String label() {
            return label;
        }

        public @Nullable String command() {
            return command;
        }

        public @Nullable String linkDialog() {
            return linkDialog;
        }
    }
}
