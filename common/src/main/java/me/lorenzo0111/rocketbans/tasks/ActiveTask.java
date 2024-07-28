package me.lorenzo0111.rocketbans.tasks;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;

public class ActiveTask implements Runnable {
    private final RocketBansPlugin plugin;

    public ActiveTask(RocketBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.getDatabase().getActive(Ban.class).thenAccept(bans -> {
            for (Ban ban : bans) {
                if (ban.expired()) ban.expire();
            }
        });

        this.plugin.getDatabase().getActive(Mute.class).thenAccept(mutes -> {
            for (Mute mute : mutes) {
                if (mute.expired()) mute.expire();
            }
        });

        this.plugin.getDatabase().getActive(Warn.class).thenAccept(warns -> {
            for (Warn warn : warns) {
                if (warn.expired()) warn.expire();
            }
        });
    }
}
