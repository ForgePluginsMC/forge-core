package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import java.util.Locale;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * /stats — full vanilla statistics readout for a player.
 *
 * <p>Shows every trackable (untyped) statistic: time, distance, combat, and
 * general counters. Parameterized statistics (per-block, per-item, per-entity)
 * are excluded since they require a Material or EntityType argument.
 *
 * <p>All values are also available programmatically via
 * {@link Player#getStatistic(Statistic)} for quest and milestone systems.
 */
public final class StatsCommand extends ForgeCommand {
    public StatsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String description() {
        return "Show a player's full vanilla statistics.";
    }

    @Override
    public String usage() {
        return "/stats [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target = resolveTarget(sender, args);
        if (target == null) {
            return;
        }
        String name = plugin.users().get(target).nickOrName(target);
        Text.send(sender, "<white>Statistics for " + Text.escape(name) + ":</white>");

        header(sender, "Time");
        time(sender, "Play time", Statistic.PLAY_ONE_MINUTE, target);
        time(sender, "Time since death", Statistic.TIME_SINCE_DEATH, target);
        time(sender, "Time since rest", Statistic.TIME_SINCE_REST, target);
        time(sender, "Total world time", Statistic.TOTAL_WORLD_TIME, target);
        time(sender, "Sneak time", Statistic.SNEAK_TIME, target);

        header(sender, "Distance");
        distance(sender, "Walked", Statistic.WALK_ONE_CM, target);
        distance(sender, "Crouched", Statistic.CROUCH_ONE_CM, target);
        distance(sender, "Sprinted", Statistic.SPRINT_ONE_CM, target);
        distance(sender, "Swum", Statistic.SWIM_ONE_CM, target);
        distance(sender, "Fallen", Statistic.FALL_ONE_CM, target);
        distance(sender, "Climbed", Statistic.CLIMB_ONE_CM, target);
        distance(sender, "Flown", Statistic.FLY_ONE_CM, target);
        distance(sender, "By minecart", Statistic.MINECART_ONE_CM, target);
        distance(sender, "By boat", Statistic.BOAT_ONE_CM, target);
        distance(sender, "By pig", Statistic.PIG_ONE_CM, target);
        distance(sender, "By horse", Statistic.HORSE_ONE_CM, target);
        distance(sender, "By elytra", Statistic.AVIATE_ONE_CM, target);
        distance(sender, "By strider", Statistic.STRIDER_ONE_CM, target);
        distance(sender, "By happy ghast", Statistic.HAPPY_GHAST_ONE_CM, target);
        distance(sender, "By nautilus", Statistic.NAUTILUS_ONE_CM, target);
        distance(sender, "Walked on water", Statistic.WALK_ON_WATER_ONE_CM, target);
        distance(sender, "Walked underwater", Statistic.WALK_UNDER_WATER_ONE_CM, target);

        header(sender, "Combat");
        count(sender, "Deaths", Statistic.DEATHS, target);
        count(sender, "Mob kills", Statistic.MOB_KILLS, target);
        count(sender, "Player kills", Statistic.PLAYER_KILLS, target);
        count(sender, "Target hits", Statistic.TARGET_HIT, target);
        damage(sender, "Damage dealt", Statistic.DAMAGE_DEALT, target);
        damage(sender, "Damage dealt (absorbed)", Statistic.DAMAGE_DEALT_ABSORBED, target);
        damage(sender, "Damage dealt (resisted)", Statistic.DAMAGE_DEALT_RESISTED, target);
        damage(sender, "Damage taken", Statistic.DAMAGE_TAKEN, target);
        damage(sender, "Damage absorbed", Statistic.DAMAGE_ABSORBED, target);
        damage(sender, "Damage blocked by shield", Statistic.DAMAGE_BLOCKED_BY_SHIELD, target);
        damage(sender, "Damage resisted", Statistic.DAMAGE_RESISTED, target);

        header(sender, "General");
        count(sender, "Jumps", Statistic.JUMP, target);
        count(sender, "Items dropped", Statistic.DROP_COUNT, target);
        count(sender, "Animals bred", Statistic.ANIMALS_BRED, target);
        count(sender, "Fish caught", Statistic.FISH_CAUGHT, target);
        count(sender, "Villagers talked to", Statistic.TALKED_TO_VILLAGER, target);
        count(sender, "Villager trades", Statistic.TRADED_WITH_VILLAGER, target);
        count(sender, "Cake slices eaten", Statistic.CAKE_SLICES_EATEN, target);
        count(sender, "Cauldrons filled", Statistic.CAULDRON_FILLED, target);
        count(sender, "Cauldrons used", Statistic.CAULDRON_USED, target);
        count(sender, "Armor cleaned", Statistic.ARMOR_CLEANED, target);
        count(sender, "Banners cleaned", Statistic.BANNER_CLEANED, target);
        count(sender, "Shulker boxes cleaned", Statistic.CLEAN_SHULKER_BOX, target);
        count(sender, "Items enchanted", Statistic.ITEM_ENCHANTED, target);
        count(sender, "Records played", Statistic.RECORD_PLAYED, target);
        count(sender, "Noteblocks played", Statistic.NOTEBLOCK_PLAYED, target);
        count(sender, "Noteblocks tuned", Statistic.NOTEBLOCK_TUNED, target);
        count(sender, "Flowers potted", Statistic.FLOWER_POTTED, target);
        count(sender, "Bells rung", Statistic.BELL_RING, target);
        count(sender, "Raids triggered", Statistic.RAID_TRIGGER, target);
        count(sender, "Raids won", Statistic.RAID_WIN, target);
        count(sender, "Times slept", Statistic.SLEEP_IN_BED, target);
        count(sender, "Straw beds slept in", Statistic.SLEEP_IN_STRAW_BED, target);
        count(sender, "Games left", Statistic.LEAVE_GAME, target);
        count(sender, "Chests opened", Statistic.CHEST_OPENED, target);
        count(sender, "Trapped chests triggered", Statistic.TRAPPED_CHEST_TRIGGERED, target);
        count(sender, "Ender chests opened", Statistic.ENDERCHEST_OPENED, target);
        count(sender, "Shulker boxes opened", Statistic.SHULKER_BOX_OPENED, target);
        count(sender, "Barrels opened", Statistic.OPEN_BARREL, target);
        count(sender, "Beacons interacted", Statistic.BEACON_INTERACTION, target);
        count(sender, "Brewing stands used", Statistic.BREWINGSTAND_INTERACTION, target);
        count(sender, "Furnaces used", Statistic.FURNACE_INTERACTION, target);
        count(sender, "Crafting tables used", Statistic.CRAFTING_TABLE_INTERACTION, target);
        count(sender, "Anvils used", Statistic.INTERACT_WITH_ANVIL, target);
        count(sender, "Blast furnaces used", Statistic.INTERACT_WITH_BLAST_FURNACE, target);
        count(sender, "Campfires used", Statistic.INTERACT_WITH_CAMPFIRE, target);
        count(sender, "Cartography tables used", Statistic.INTERACT_WITH_CARTOGRAPHY_TABLE, target);
        count(sender, "Grindstones used", Statistic.INTERACT_WITH_GRINDSTONE, target);
        count(sender, "Lecterns used", Statistic.INTERACT_WITH_LECTERN, target);
        count(sender, "Looms used", Statistic.INTERACT_WITH_LOOM, target);
        count(sender, "Smithing tables used", Statistic.INTERACT_WITH_SMITHING_TABLE, target);
        count(sender, "Smokers used", Statistic.INTERACT_WITH_SMOKER, target);
        count(sender, "Stonecutters used", Statistic.INTERACT_WITH_STONECUTTER, target);
        count(sender, "Dispensers inspected", Statistic.DISPENSER_INSPECTED, target);
        count(sender, "Droppers inspected", Statistic.DROPPER_INSPECTED, target);
        count(sender, "Hoppers inspected", Statistic.HOPPER_INSPECTED, target);
    }

    private @Nullable Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!sender.hasPermission("forgecore.stats.others")) {
                Text.error(sender, "You don't have permission to do that.");
                return null;
            }
            return Players.find(sender, args[0]);
        }
        Player player = asPlayer(sender);
        if (player == null) {
            Text.error(sender, "Only players can use that command.");
            return null;
        }
        return player;
    }

    private static void header(CommandSender sender, String title) {
        Text.send(sender, "<gold><bold>" + title + "</bold></gold>");
    }

    private static void stat(CommandSender sender, String label, String value) {
        Text.send(sender, "<gray>" + label + ":</gray> <white>" + Text.escape(value) + "</white>");
    }

    private static void time(CommandSender sender, String label, Statistic stat, Player target) {
        stat(sender, label, Time.format(target.getStatistic(stat) / 20L));
    }

    private static void distance(CommandSender sender, String label, Statistic stat, Player target) {
        stat(sender, label, String.format(Locale.ROOT, "%.1f km",
                target.getStatistic(stat) / 100000.0));
    }

    private static void damage(CommandSender sender, String label, Statistic stat, Player target) {
        stat(sender, label, String.format(Locale.ROOT, "%.1f", target.getStatistic(stat) / 10.0));
    }

    private static void count(CommandSender sender, String label, Statistic stat, Player target) {
        stat(sender, label, String.valueOf(target.getStatistic(stat)));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.stats.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
