package com.beastsmc.bungeemutes;

import com.beastsmc.bungeemutes.commands.BanchatCommand;
import com.beastsmc.bungeemutes.commands.MuteCommand;
import com.beastsmc.bungeemutes.commands.UnmuteCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;

public class BungeeMutes extends JavaPlugin {
    public boolean debug;

    public MuteCommand muteCommandHandler;
    public BanchatCommand banchatCommandHandler;
    public UnmuteCommand unmuteCommandHandler;
    public Storage storage;
    public MuteSyncer syncer;
    public static BungeeMutes instance;


    public void onEnable() {
        instance = this;
        loadConfig();
        setupDatabase();
        storage = new Storage(this);
        muteCommandHandler = new MuteCommand(this);
        muteCommandHandler.setup();
        banchatCommandHandler = new BanchatCommand(this);
        banchatCommandHandler.setup();
        unmuteCommandHandler = new UnmuteCommand(this);
        unmuteCommandHandler.setup();

        syncer = new MuteSyncer(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", syncer);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void loadConfig() {
        this.saveDefaultConfig();
        this.debug = this.getConfig().getBoolean("debug");

    }

    private void setupDatabase() {
        try {
            getDatabase().find(Mute.class).findRowCount();
        } catch (PersistenceException ex) {
            getLogger().info("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }

    public static void alertTargetMute(Player p, Mute mute) {
        p.sendMessage(ChatColor.RED + "You have been "
                      + (mute.isPermanent() ? "banned from chat" : "muted" )
                      + " by " + ChatColor.YELLOW + mute.getMuterName());
        p.sendMessage(ChatColor.RED + "Duration: " + ChatColor.YELLOW
                      + (mute.isPermanent() ? "forever" : MuteCommand.expirationToFormattedTime(mute.getExpiration())));
        if(mute.getReason()!=null) {
            p.sendMessage(ChatColor.RED + "Reason: " + ChatColor.YELLOW + mute.getReason());
        }
    }

    public static void alertTargetUnmute(Player p) {
        p.sendMessage(ChatColor.GREEN + "You have been unmuted! Please behave.");
    }

    public static void alertIssuerMute(CommandSender sender, String targetName, Mute mute) {


        sender.sendMessage(ChatColor.RED + "You have muted " + ChatColor.YELLOW + targetName);
        sender.sendMessage(ChatColor.RED + "Duration: " + ChatColor.YELLOW
                      + (mute.isPermanent() ? "forever" : MuteCommand.expirationToFormattedTime(mute.getExpiration())));
        if(mute.getReason()!=null) {
            sender.sendMessage(ChatColor.RED + "Reason: " + ChatColor.YELLOW + mute.getReason());
        }
    }

    public static void alertIssuerAlreadyMuted(CommandSender sender, String targetName, Mute mute) {
        sender.sendMessage(ChatColor.RED + "Player " + ChatColor.YELLOW + targetName +
                ChatColor.RED + " is already muted for " + ChatColor.YELLOW + MuteCommand.expirationToFormattedTime(mute.getExpiration()));
    }

    public static void alertNoMuteExists(CommandSender sender, String targetName) {
        sender.sendMessage(ChatColor.RED + "Player " + ChatColor.YELLOW + targetName + ChatColor.RED + " is not muted!");
    }

    public static void error(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + "[ERROR]" + message);
    }

    public static void alertIssuerUnmuted(CommandSender sender, String username) {
        sender.sendMessage(ChatColor.GREEN + "You have unmuted " + username);
    }
}
