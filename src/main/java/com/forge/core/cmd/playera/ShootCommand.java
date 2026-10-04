package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.bukkit.util.Vector;

/** Fire any projectile type from your position. */
public final class ShootCommand extends PlayerACommand {
    public ShootCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "shoot";
    }

    @Override
    public String description() {
        return "Shoot any projectile type.";
    }

    @Override
    public String usage() {
        return "/shoot <arrow|spectral_arrow|tipped_arrow|trident|snowball|egg|enderpearl|fireball|small_fireball|dragon_fireball|wither_skull|shulker_bullet>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = (Player) sender;
        Class<? extends Projectile> type = switch (args[0].toLowerCase(Locale.ROOT)) {
            case "arrow", "tipped_arrow" -> org.bukkit.entity.Arrow.class;
            case "spectral_arrow" -> SpectralArrow.class;
            case "trident" -> Trident.class;
            case "snowball" -> Snowball.class;
            case "egg" -> Egg.class;
            case "enderpearl", "ender_pearl" -> EnderPearl.class;
            case "fireball", "large_fireball" -> LargeFireball.class;
            case "small_fireball" -> SmallFireball.class;
            case "dragon_fireball" -> DragonFireball.class;
            case "wither_skull" -> WitherSkull.class;
            case "shulker_bullet" -> ShulkerBullet.class;
            default -> null;
        };
        if (type == null) {
            Text.error(sender, "Unknown projectile. See " + usage());
            return;
        }
        Vector dir = player.getEyeLocation().getDirection().multiply(2.0);
        Projectile projectile = player.launchProjectile(type, dir);
        // Fireballs need explicit direction/velocity
        if (projectile instanceof Fireball fireball) {
            fireball.setDirection(dir);
        }
        Text.ok(sender, "Fired <white>" + Text.escape(args[0].toLowerCase(Locale.ROOT)) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            List<String> types = List.of("arrow", "spectral_arrow", "trident",
                    "snowball", "egg", "enderpearl", "fireball", "small_fireball",
                    "dragon_fireball", "wither_skull", "shulker_bullet");
            String last = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return types.stream().filter(t -> t.startsWith(last)).toList();
        }
        return List.of();
    }
}
