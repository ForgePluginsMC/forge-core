package com.forge.core.cmd.systemsa.sc;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

/**
 * Sign copy/paste sessions for {@code /sc}. While copy mode is on,
 * right-clicking a sign copies its four lines; right-clicking another sign
 * pastes them. Clipboard lives for the session only.
 */
public final class SignCopyManager implements Listener {
    private static @Nullable SignCopyManager instance;

    /** Global accessor. */
    public static SignCopyManager get() {
        if (instance == null) {
            throw new IllegalStateException("SignCopyManager not initialized");
        }
        return instance;
    }

    private final Set<UUID> copyMode = new HashSet<>();
    private final Map<UUID, String[]> clipboard = new HashMap<>();

    public SignCopyManager(ForgeCore plugin) {
        instance = this;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Toggle copy mode; returns the new state. */
    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (copyMode.remove(uuid)) {
            clipboard.remove(uuid);
            return false;
        }
        copyMode.add(uuid);
        return true;
    }

    public boolean isCopying(Player player) {
        return copyMode.contains(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!EquipmentSlot.HAND.equals(event.getHand())) {
            return;
        }
        Player player = event.getPlayer();
        if (!copyMode.contains(player.getUniqueId())) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign sign)) {
            return;
        }
        event.setCancelled(true);
        String[] clip = clipboard.get(player.getUniqueId());
        SignSide front = sign.getSide(Side.FRONT);
        if (clip == null) {
            String[] lines = new String[4];
            for (int i = 0; i < 4; i++) {
                lines[i] = PlainTextComponentSerializer.plainText().serialize(front.line(i));
            }
            clipboard.put(player.getUniqueId(), lines);
            Text.ok(player, "Sign copied — right-click another sign to paste it.");
        } else {
            for (int i = 0; i < 4; i++) {
                front.line(i, Component.text(clip[i]));
            }
            sign.update();
            Text.ok(player, "Sign pasted.");
        }
    }
}
