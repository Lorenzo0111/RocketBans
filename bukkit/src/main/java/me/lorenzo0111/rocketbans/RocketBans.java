package me.lorenzo0111.rocketbans;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.listeners.PlayerListener;
import me.lorenzo0111.rocketbans.managers.MuteManager;
import me.lorenzo0111.rocketbans.tasks.ActiveTask;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class RocketBans extends JavaPlugin implements RocketBansAPI {
    private static RocketBans instance;
    private boolean firstRun = true;
    private SQLHandler database;
    private MuteManager muteManager;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        instance = this;

        if (new File(this.getDataFolder(), "config.yml").exists())
            this.firstRun = false;

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.database = new SQLHandler(this);
        this.muteManager = new MuteManager(this);

        this.reload();
        this.firstRun = false;

        // ******** Commands ********
        this.getCommand("rocketbans").setExecutor(new RocketBansCommand(this));

        // ******** Listeners ********
        this.getServer().getPluginManager().registerEvents(this.muteManager, this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // ******** Tasks ********
        new ActiveTask(this);
    }


    @Override
    public void onDisable() {
        try {
            this.getDatabase().close();
        } catch (Exception e) {
            this.logException(e);
        }
    }

    public void log(String message) {
        ConsoleCommandSender logger = Bukkit.getConsoleSender();
        logger.sendMessage(StringUtils.color(getMessage("prefix") + message));
    }

    public void logException(Throwable e) {
        this.getLogger().log(Level.SEVERE, "An unexpected error occurred", e);
    }

    public String getMessage(String path, boolean messagesSection) {
        return StringUtils.color(
                this.getConfig().getString(messagesSection ? "messages." + path : path, "&cUnable to find the following key: &7" + path + "&c.")
        );
    }

    public List<String> getMessages(String path, boolean messagesSection) {
        return StringUtils.color(
                this.getConfig().getStringList(messagesSection ? "messages." + path : path)
        );
    }

    public String getMessage(String path) {
        return getMessage(path, true);
    }

    public List<String> getMessages(String path) {
        return getMessages(path, true);
    }

    public String getPrefixed(String path) {
        return getMessage("prefix") + StringUtils.color(
                this.getConfig().getString("messages." + path, "&cUnable to find the following key: &7" + path + "&c.")
        );
    }

    public void reload() {
        this.reloadConfig();
        this.log("&c&m---------------------------------------------------");
        this.log("             &c&lRocket&e&lBans &7v" + this.getDescription().getVersion());
        this.log(" ");

        if (firstRun) {
            this.log(" &7Thanks for installing the plugin.");
            this.log(" &7We detected that this is the first time you run the plugin.");
            this.log(" &7Please, configure the plugin in the config.yml file.");
            this.log(" &7When you are done, run &c&n/rocketbans reload&7 to reload the plugin.");
        } else {
            try {
                this.database.init();
                this.muteManager.reload();
                this.log(" &cDatabase connection&7: &a&lSUCCESS");
            } catch (Exception e) {
                this.log(" &cAn error occurred while connecting to the database.");
                this.log(" &cPlease, check your configuration and try again.");
                this.log(" &cIf the problem persists, contact the developer.");
                this.log(" &cError: " + e.getMessage());

            }
        }

        this.log("&c&m---------------------------------------------------");
    }

    public static RocketBans getInstance() {
        return instance;
    }

    public SQLHandler getDatabase() {
        return database;
    }

    @Override
    public MuteManager getMuteManager() {
        return muteManager;
    }

    @Override
    public <T extends HistoryRecord> void punish(T item) {
        database.add(item);

        if (item instanceof Mute)
            muteManager.addMute((Mute) item);

        if (item instanceof Ban ban)
            ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE))
                    .addBan(
                            Bukkit.getOfflinePlayer(item.uuid()).getPlayerProfile(),
                            item.reason(),
                            ban.expires(),
                            ban.executor().toString()
                    );
    }

    @Override
    public <T extends HistoryRecord> void expire(Class<T> type, UUID uuid) {
        database.expireAll(type, uuid);

        if (type.equals(Mute.class))
            muteManager.removeMutes(uuid);

        if (type.equals(Ban.class))
            ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE))
                    .pardon(Bukkit.getOfflinePlayer(uuid).getPlayerProfile());
    }

    @Override
    public <T extends HistoryRecord> void expire(Class<T> type, int id) {
        database.expireSingle(type, id);

        if (type.equals(Mute.class))
            muteManager.getMutes().values()
                    .stream().filter(m -> m.id() == id)
                    .findFirst().ifPresent(mute -> muteManager.removeMutes(mute.uuid()));
    }

    @Override
    public CompletableFuture<List<HistoryRecord>> history(@Nullable Class<? extends HistoryRecord> type, UUID uuid) {
        if (type != null)
            return database.get(type, uuid, false)
                    .thenApply(ArrayList::new);

        return CompletableFuture.supplyAsync(() -> {
            List<HistoryRecord> records = new ArrayList<>();

            records.addAll(database.get(Ban.class, uuid, false).join());
            records.addAll(database.get(Mute.class, uuid, false).join());
            records.addAll(database.get(Warn.class, uuid, false).join());
            records.addAll(database.get(Kick.class, uuid, false).join());

            return records;
        }, database.getExecutor());
    }
}
