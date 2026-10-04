package com.forge.core;

import com.forge.core.afk.AfkManager;
import com.forge.core.chat.MuteManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.data.JailManager;
import com.forge.core.data.KitManager;
import com.forge.core.data.UserManager;
import com.forge.core.data.WarpManager;
import com.forge.core.economy.EconomyManager;
import com.forge.core.help.HelpManager;
import com.forge.core.punish.BanManager;
import com.forge.core.teleport.TpaManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ForgeCore — one plugin to run the server: homes, warps, kits, economy,
 * moderation, teleport requests, player utilities and server systems.
 */
public final class ForgeCore extends JavaPlugin {
    private static ForgeCore instance;

    private UserManager users;
    private WarpManager warps;
    private KitManager kits;
    private EconomyManager economy;
    private JailManager jails;
    private TpaManager tpa;
    private MuteManager mutes;
    private BanManager bans;
    private AfkManager afk;
    private HelpManager help;
    private com.forge.core.cmd.playerb.SavedItemsManager savedItems;

    /** Global accessor for command implementations. */
    public static ForgeCore get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        users = new UserManager(this);
        warps = new WarpManager(this);
        kits = new KitManager(this);
        economy = new EconomyManager(this);
        jails = new JailManager(this);
        tpa = new TpaManager(this);
        mutes = new MuteManager(this);
        bans = new BanManager(this);
        afk = new AfkManager(this);
        savedItems = new com.forge.core.cmd.playerb.SavedItemsManager(this);

        getServer().getPluginManager().registerEvents(new CoreListener(this), this);
        help = new HelpManager(this);
        CommandRegistry.registerAll(this);
        com.forge.core.economy.VaultHook.init(this);

        getLogger().info("ForgeCore enabled: " + CommandRegistry.count() + " commands registered.");
    }

    @Override
    public void onDisable() {
        if (users != null) {
            users.saveAll();
        }
        if (warps != null) {
            warps.save();
        }
        if (kits != null) {
            kits.save();
        }
        if (economy != null) {
            economy.save();
        }
        if (jails != null) {
            jails.save();
        }
        if (bans != null) {
            bans.save();
        }
        instance = null;
    }

    public UserManager users() {
        return users;
    }

    public WarpManager warps() {
        return warps;
    }

    public KitManager kits() {
        return kits;
    }

    public EconomyManager economy() {
        return economy;
    }

    public JailManager jails() {
        return jails;
    }

    public TpaManager tpa() {
        return tpa;
    }

    public MuteManager mutes() {
        return mutes;
    }

    public BanManager bans() {
        return bans;
    }

    public AfkManager afk() {
        return afk;
    }

    public HelpManager help() {
        return help;
    }

    public com.forge.core.cmd.playerb.SavedItemsManager savedItems() {
        return savedItems;
    }
}
