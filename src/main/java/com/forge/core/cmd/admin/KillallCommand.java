package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Kill entities by category within a radius.
 * Types: monster, animal, all, drops, arrows, minecarts, boats, armorstands, items.
 */
public final class KillallCommand extends ForgeCommand {
    public KillallCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "killall";
    }

    @Override
    public List<String> aliases() {
        return List.of("killmobs");
    }

    @Override
    public String description() {
        return "Kill entities of a type within a radius.";
    }

    @Override
    public String usage() {
        return "/killall <monster|animal|all|drops|arrows|minecarts|boats|armorstands|items> [radius]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        String type = args[0].toLowerCase(Locale.ROOT);
        double radius = 50.0;
        if (args.length >= 2) {
            try {
                radius = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                Text.error(sender, "Radius must be a number.");
                return;
            }
            if (radius < 1 || radius > 500) {
                Text.error(sender, "Radius must be between 1 and 500.");
                return;
            }
        }
        double radiusSq = radius * radius;
        int killed = 0;
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Player) {
                continue;
            }
            if (entity.getLocation().distanceSquared(player.getLocation()) > radiusSq) {
                continue;
            }
            if (!matches(type, entity)) {
                continue;
            }
            if (entity instanceof LivingEntity living) {
                living.setHealth(0.0);
            } else {
                entity.remove();
            }
            killed++;
        }
        Text.ok(sender, "Killed <white>" + killed + "</white> entities of type <white>"
                + Text.escape(type) + "</white> within " + radius + " blocks.");
    }

    private static boolean matches(String type, Entity entity) {
        EntityType et = entity.getType();
        return switch (type) {
            case "monster", "monsters", "mob", "mobs", "hostile" ->
                    et.getEntityClass() != null && org.bukkit.entity.Monster.class.isAssignableFrom(et.getEntityClass());
            case "animal", "animals", "passive" ->
                    et.getEntityClass() != null && org.bukkit.entity.Animals.class.isAssignableFrom(et.getEntityClass());
            case "all", "everything" -> true;
            case "drops", "items", "item" -> et == EntityType.ITEM;
            case "arrows", "arrow", "projectiles" ->
                    entity instanceof org.bukkit.entity.Projectile;
            case "minecarts", "minecart" ->
                    entity instanceof org.bukkit.entity.Minecart;
            case "boats", "boat" ->
                    entity instanceof org.bukkit.entity.Boat;
            case "armorstands", "armorstand" ->
                    et == EntityType.ARMOR_STAND;
            default -> false;
        };
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> types = List.of("monster", "animal", "all", "drops", "arrows",
                    "minecarts", "boats", "armorstands", "items");
            String last = args[0].toLowerCase(Locale.ROOT);
            return types.stream().filter(t -> t.startsWith(last)).toList();
        }
        return List.of();
    }
}
