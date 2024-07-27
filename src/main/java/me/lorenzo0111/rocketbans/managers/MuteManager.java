package me.lorenzo0111.rocketbans.managers;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.managers.IMuteManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager implements Listener, IMuteManager {
    private final RocketBans plugin;
    private final Map<UUID, Mute> activeMutes = new HashMap<>();

    public MuteManager(RocketBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        plugin.getDatabase().getActive(Mute.class).thenAccept(mutes -> {
            for (Mute mute : mutes) {
                activeMutes.put(mute.uuid(), mute);
            }
        });
    }

    @Override
    public void addMute(Mute mute) {
        activeMutes.put(mute.uuid(), mute);
    }

    @Override
    public void removeMutes(UUID uuid) {
        activeMutes.remove(uuid);
    }

    public Map<UUID, Mute> getMutes() {
        return activeMutes;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!activeMutes.containsKey(event.getPlayer().getUniqueId())) return;

        Mute mute = activeMutes.get(event.getPlayer().getUniqueId());
        if (mute.expired() && mute.active())
            mute.expire();

        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getPrefixed("mute.deny"));
    }
}
