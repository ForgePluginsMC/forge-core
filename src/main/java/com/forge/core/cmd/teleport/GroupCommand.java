package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /group [player] — show permission groups. Uses LuckPerms via reflection
 * when present; otherwise reports that no permission data is available.
 */
public final class GroupCommand extends TeleportCommand {
    public GroupCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "group";
    }

    @Override
    public List<String> aliases() {
        return List.of("groups");
    }

    @Override
    public String description() {
        return "Show a player's permission groups.";
    }

    @Override
    public String usage() {
        return "/group [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        UUID uuid;
        String displayName;
        if (args.length == 0) {
            Player player = requirePlayer(sender);
            uuid = player.getUniqueId();
            displayName = player.getName();
        } else {
            OfflinePlayer offline = Players.offline(args[0]);
            if (offline == null) {
                throw fail("Player <white>" + Text.escape(args[0]) + "</white> has never played here.");
            }
            uuid = offline.getUniqueId();
            displayName = offline.getName() == null ? args[0] : offline.getName();
        }
        GroupInfo info = readLuckPerms(uuid);
        if (info == null) {
            Text.send(sender, "<gray>No permission plugin data is available"
                    + " (install LuckPerms for group info).");
            return;
        }
        String groups = info.groups().isEmpty() ? "<gray>none" : "<white>"
                + Text.escape(String.join("<gray>, <white>", info.groups()));
        Text.send(sender, "<gray>Groups of <white>" + Text.escape(displayName) + "</white>: " + groups
                + "<gray> (primary: <white>" + Text.escape(info.primary()) + "<gray>).");
    }

    /** LuckPerms data via reflection; null when LuckPerms is absent or fails. */
    private static @org.jspecify.annotations.Nullable GroupInfo readLuckPerms(UUID uuid) {
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = providerClass.getMethod("get").invoke(null);
            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) {
                return null;
            }
            Class<?> nodeTypeClass = Class.forName("net.luckperms.api.node.NodeType");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object inheritance = Enum.valueOf((Class<Enum>) nodeTypeClass.asSubclass(Enum.class), "INHERITANCE");
            Method getNodes = user.getClass().getMethod("getNodes", nodeTypeClass);
            @SuppressWarnings("unchecked")
            Collection<Object> nodes = (Collection<Object>) getNodes.invoke(user, inheritance);
            List<String> groups = new ArrayList<>();
            for (Object node : nodes) {
                String groupName = (String) node.getClass().getMethod("getGroupName").invoke(node);
                if (!groups.contains(groupName)) {
                    groups.add(groupName);
                }
            }
            groups.sort(String.CASE_INSENSITIVE_ORDER);
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            String primary = (String) metaData.getClass().getMethod("getPrimaryGroup").invoke(metaData);
            return new GroupInfo(groups, primary == null ? "default" : primary);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private record GroupInfo(List<String> groups, String primary) {
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
