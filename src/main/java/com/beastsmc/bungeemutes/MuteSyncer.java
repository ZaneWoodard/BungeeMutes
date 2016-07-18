package com.beastsmc.bungeemutes;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class MuteSyncer implements PluginMessageListener {
    private final BungeeMutes plugin;

    public MuteSyncer(BungeeMutes plugin) {
        this.plugin = plugin;
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if(!subchannel.equals("BungeeMutes")) return;

        plugin.getLogger().info("Received BungeeMutes message");

        short len = in.readShort();

        String action = in.readUTF();
        plugin.getLogger().info("Read action: " + action);
        String strUUID = in.readUTF();
        plugin.getLogger().info("Read UUID: " + strUUID);
        if(action.equals("Mute")) {
            Player target = Bukkit.getPlayer(UUID.fromString(strUUID));
            if (target == null) return;
            Mute mute = plugin.storage.fetchMute(strUUID);
            BungeeMutes.alertTargetMute(target, mute);
            if(this.plugin.debug) this.plugin.getLogger().info("Got mute: " + mute);
        } else if(action.equals("Unmute")) {
            Player target = Bukkit.getPlayer(UUID.fromString(strUUID));
            if(target!=null) {
                BungeeMutes.alertTargetUnmute(target);
                if(this.plugin.debug) this.plugin.getLogger().info("Got unmute for: " + target.getName());
            } else {
                if(this.plugin.debug) this.plugin.getLogger().info("Got unmute for offline target!");
            }
            plugin.storage.removeFromCache(strUUID);
        }
    }

    public void sendMute(String target, Mute mute) {
        plugin.getLogger().info("Sending mute for " + mute.getMutedUUID());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ForwardToPlayer");
        out.writeUTF(target);
        out.writeUTF("BungeeMutes");

        ByteArrayDataOutput submsg = ByteStreams.newDataOutput();
        submsg.writeUTF("Mute");
        submsg.writeUTF(mute.getMutedUUID());

        out.writeShort(submsg.toByteArray().length);
        out.write(submsg.toByteArray());

        Player player = Bukkit.getOnlinePlayers().iterator().next();
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public void sendUnmute(Mute mute) {
        plugin.getLogger().info("Sending unmute for " + mute.getMutedUUID());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("BungeeMutes");

        ByteArrayDataOutput submsg = ByteStreams.newDataOutput();
        submsg.writeUTF("Unmute");
        submsg.writeUTF(mute.getMutedUUID());

        out.writeShort(submsg.toByteArray().length);
        out.write(submsg.toByteArray());

        Player player = Bukkit.getOnlinePlayers().iterator().next();
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public void broadcastMute(String username) {
        plugin.getLogger().info("Broadcasting mute for " + username);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerList");
        out.writeUTF("ALL");

    }
}
