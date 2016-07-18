package com.beastsmc.bungeemutes.commands;

import com.beastsmc.bungeemutes.BungeeMutes;
import com.beastsmc.bungeemutes.Mute;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements CommandExecutor {
    private final BungeeMutes plugin;

    public MuteCommand(BungeeMutes plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        plugin.getCommand("mute").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String strCommand, String[] args) {
        if(!strCommand.equalsIgnoreCase("mute")) return false;
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
                if(existingMute.isExpired()) {
                    existingMute.remove();
                } else {
                    BungeeMutes.alertIssuerAlreadyMuted(sender, username, existingMute);
                    return true;
                }
            }

            Calendar expirationCal = Calendar.getInstance();
            String reason = null;
            if(args.length >= 2 ) {
                if(args[1].matches("\\d*(s|m|h|d)")) {
                    char unit = args[1].charAt(args[1].length()-1);
                    int time = Integer.parseInt(args[1].substring(0, args[1].length()-1));
                    expirationCal.add(Calendar.SECOND, (int)timeInputToSeconds(time, unit));
                    if(args.length>=3) {
                        reason = retrieveReason(2, args);
                    }
                } else {
                    //No time specified, fetch reason
                    expirationCal.add(Calendar.HOUR_OF_DAY, 1); //Default duration
                    reason = retrieveReason(1, args);
                }
            } else {
                //No time or reason specified
                expirationCal.add(Calendar.HOUR_OF_DAY, 1); //Default duration
            }

            Date expiration = expirationCal.getTime();
            Mute mute = new Mute(uuid.toString(), sender.getName(), expiration, reason);
            plugin.storage.storeMute(mute);


            if(player.isOnline()) BungeeMutes.alertTargetMute((Player) player, mute);
            else plugin.syncer.sendMute(player.getName(), mute);

            BungeeMutes.alertIssuerMute(sender, username, mute);
            return true;

        } else {
            BungeeMutes.error(sender, "You have not supplied the right command arguments! Need exactly one(target username)");
        }
        return false;
    }

    public static String retrieveReason(int startIndex, String[] args) {
        StringBuilder reasonBuilder = new StringBuilder();
        for(int i = startIndex; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        return reasonBuilder.toString();
    }

    public static long timeInputToSeconds(int time, char unit) {
        long millis = time;
        switch(unit) {
            case 'd':  millis*=24;
            case 'h':  millis*=60;
            case 'm':  millis*=60;
            case 's':  millis*=1;
        }
        return millis;
    }

    public static String expirationToFormattedTime(Date expirationDate) {
        long duration = expirationDate.getTime() - System.currentTimeMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
    }


}
