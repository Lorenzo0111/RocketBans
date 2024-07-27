package me.lorenzo0111.rocketbans.tasks;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.data.Ban;
import me.lorenzo0111.rocketbans.data.Mute;
import org.bukkit.scheduler.BukkitRunnable;

public class ActiveTask extends BukkitRunnable {
    private final RocketBans plugin;

    public ActiveTask(RocketBans plugin) {
        this.plugin = plugin;

        this.runTaskTimerAsynchronously(plugin, 0, 60 * 60 * 20L);
    }

    @Override
    public void run() {
        this.plugin.getDatabase().getActiveBans().thenAccept(bans -> {
            for (Ban ban : bans) {
                if (ban.expired()) ban.expire();
            }
        });

        this.plugin.getDatabase().getActiveMutes().thenAccept(bans -> {
            for (Mute mute : bans) {
                if (mute.expired()) mute.expire();
            }
        });
    }
}
