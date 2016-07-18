package com.beastsmc.bungeemutes.commands;

import com.beastsmc.bungeemutes.BungeeMutes;
import com.beastsmc.bungeemutes.Mute;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {
    private final BungeeMutes plugin;

    public UnmuteCommand(BungeeMutes plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        plugin.getCommand("unmute").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String strCommand, String[] args) {
        if(!strCommand.equalsIgnoreCase("unmute")) return false;
        if(args.length == 1) {
            //Get playername
            String username = args[0];
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);

            if(!player.isOnline() && !player.hasPlayedBefore()) {
                BungeeMutes.error(sender, "Player " + username + " has never played before!");
                return false;
            }

            UUID uuid = player.getUniqueId();

            Mute mute = BungeeMutes.instance.storage.fetchMute(uuid.toString());
            if(mute==null) {
                BungeeMutes.alertNoMuteExists(sender, username);
            } else {
                mute.remove();
                if(player.isOnline()) BungeeMutes.alertTargetUnmute((Player) player);
                BungeeMutes.alertIssuerUnmuted(sender, username);
            }
            return true;

        } else {
            BungeeMutes.error(sender, "You have not supplied the right command arguments! Need exactly one(target username)");
        }
        return false;
    }
}
