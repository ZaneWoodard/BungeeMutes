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

public class BanchatCommand implements CommandExecutor {
    private final BungeeMutes plugin;

    public BanchatCommand(BungeeMutes plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        plugin.getCommand("banchat").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String strCommand, String[] args) {
        if(!strCommand.equalsIgnoreCase("banchat")) return false;
        if(args.length >= 1) {
            //Get playername
            String username = args[0];
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);

            if(!player.isOnline() && !player.hasPlayedBefore()) {
                BungeeMutes.error(sender, "Player " + username + " has never played before!");
                return false;
            }

            UUID uuid = player.getUniqueId();

            Mute existingMute = BungeeMutes.instance.storage.fetchMute(uuid.toString());
            if(existingMute!=null) {
                existingMute.remove();
            }

            String reason = null;
            if(args.length >= 2 ) {
                reason = MuteCommand.retrieveReason(1, args);
            }

            Mute mute = new Mute(uuid.toString(), sender.getName(), null, reason);
            plugin.storage.storeMute(mute);

            if(player.isOnline()) BungeeMutes.alertTargetMute((Player) player, mute);
            else plugin.syncer.sendMute(player.getName(), mute);

            BungeeMutes.alertIssuerMute(sender, username, mute);
            return true;

        } else {
            BungeeMutes.error(sender, "You have not supplied the right command arguments! Need player target and optional reason");
        }
        return false;
    }

}