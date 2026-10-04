package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * Send the ForgeCore UI resource pack to a player.
 *
 * <p>Usage: /resourcepack — sends the configured pack URL to you.
 */
@NullMarked
public final class ResourcepackCommand extends ForgeCommand {
    public ResourcepackCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "resourcepack";
    }

    @Override
    public List<String> aliases() {
        return List.of("rp", "pack");
    }

    @Override
    public String description() {
        return "Get the ForgeCore UI resource pack.";
    }

    @Override
    public String usage() {
        return "/resourcepack";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        String url = plugin.getConfig().getString("resourcepack-url", "");
        if (url.isBlank()) {
            Text.error(sender,
                    "No resource pack URL configured. An admin can set <white>resourcepack-url</white> in config.yml.");
            return;
        }
        String hashHex = plugin.getConfig().getString("resourcepack-hash", "");
        byte[] hash = hashHex.isBlank() ? new byte[0] : hexToBytes(hashHex);
        Text.send(sender, "<gold>Sending ForgeCore UI resource pack...");
        // Use URL-based UUID so client treats each pack version as distinct.
        // Same UUID + different URL = client uses cached pack.
        java.util.UUID packId = java.util.UUID.nameUUIDFromBytes(
                url.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        net.kyori.adventure.text.Component prompt =
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<gold>ForgeCore UI <gray>— dark dashboard theme");
        player.setResourcePack(packId, url, hash, prompt, true);
        Text.ok(sender, "Resource pack sent! The UI will update once it loads.");
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
