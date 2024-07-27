package me.lorenzo0111.rocketbans.tasks;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import org.bukkit.scheduler.BukkitRunnable;

public class ActiveTask extends BukkitRunnable {
    private final RocketBans plugin;

    public ActiveTask(RocketBans plugin) {
        this.plugin = plugin;

        this.runTaskTimerAsynchronously(plugin, 0, 60 * 60 * 20L);
    }

    @Override
    public void run() {
        this.plugin.getDatabase().getActive(Ban.class).thenAccept(bans -> {
            for (Ban ban : bans) {
                if (ban.expired()) ban.expire();
            }
        });

        this.plugin.getDatabase().getActive(Mute.class).thenAccept(bans -> {
            for (Mute mute : bans) {
                if (mute.expired()) mute.expire();
            }
        });
    }
}
