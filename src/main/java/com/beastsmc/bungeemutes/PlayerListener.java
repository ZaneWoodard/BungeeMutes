package com.beastsmc.bungeemutes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final BungeeMutes plugin;

    public PlayerListener(BungeeMutes plugin) {
        this.plugin = plugin;
    }

    /**
     * Blocks muted messages, removes expired bans on chat
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if(event.isCancelled()) return;
        UUID uuid = event.getPlayer().getUniqueId();
        Mute mute = plugin.storage.retrieveMuteFromCache(uuid.toString());
        if(mute==null) return;
        if(mute.isExpired()) {
            mute.remove();
            BungeeMutes.alertTargetUnmute(event.getPlayer());
        } else {
            event.setCancelled(true);
            BungeeMutes.alertTargetMute(event.getPlayer(), mute);
        }
    }

    /**
     * Pre-loads the mute record, asynchronously
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Mute mute = plugin.storage.fetchMute(event.getUniqueId().toString());
        if(mute!=null && mute.isExpired()) {
            mute.remove();
        }
    }

    /**
     * Checks to see if player is muted, alerts on login
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Mute mute = plugin.storage.retrieveMuteFromCache(uuid.toString()); //Pre-cached by prelogin
        if(mute==null) return;
        BungeeMutes.alertTargetMute(event.getPlayer(), mute);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.storage.removeFromCache(uuid.toString());
    }
}
