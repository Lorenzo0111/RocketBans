package me.lorenzo0111.rocketbans.bukkit;

import me.lorenzo0111.rocketbans.bukkit.listeners.ChannelListener;
import me.lorenzo0111.rocketbans.bukkit.listeners.PlayerListener;
import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.bukkit.commands.BukkitCommand;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.managers.MuteManager;
import me.lorenzo0111.rocketbans.bukkit.platform.BukkitPlatform;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import me.lorenzo0111.rocketbans.tasks.ActiveTask;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class RocketBans extends JavaPlugin implements RocketBansPlugin {
    private PlatformAdapter platform;
    private ConfigurationNode config;
    private boolean firstRun = true;
    private SQLHandler database;
    private MuteManager muteManager;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        RocketBansProvider.set(this);

        this.platform = new BukkitPlatform(this);

        if (new File(this.getDataFolder(), "config.yml").exists())
            this.firstRun = false;

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        new Metrics(this, 22812);

        this.database = new SQLHandler(this);
        this.muteManager = new MuteManager(this);

        this.reload();
        this.firstRun = false;

        // ******** Commands ********
        this.getCommand("rocketbans").setExecutor(new BukkitCommand(this));

        // ******** Listeners ********
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // ******** Tasks ********
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ActiveTask(this), 0, 60 * 60 * 20L);

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "rocketbans:sync", new ChannelListener(this));
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "rocketbans:sync");
    }


    @Override
    public void onDisable() {
        try {
            this.getDatabase().close();
        } catch (Exception e) {
            platform.logException(e);
        }
    }

    public void log(String message) {
        ConsoleCommandSender logger = Bukkit.getConsoleSender();
        logger.sendMessage(StringUtils.color(getMessage("prefix") + message));
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

    @Override
    public String getMessage(String path) {
        return getMessage(path, true);
    }

    @Override
    public List<String> getMessages(String path) {
        return getMessages(path, true);
    }

    @Override
    public String getPrefixed(String path) {
        return getMessage("prefix") + StringUtils.color(
                this.getConfig().getString("messages." + path, "&cUnable to find the following key: &7" + path + "&c.")
        );
    }

    @Override
    public void reload() {
        this.reloadConfig();

        try {
            this.config = YamlConfigurationLoader.builder()
                    .file(new File(this.getDataFolder(), "config.yml"))
                    .build()
                    .load();
        } catch (ConfigurateException e) {
            platform.logException(e);
        }

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

    @Override
    public String getVersion() {
        return this.getDescription().getVersion();
    }

    @Override
    public SQLHandler getDatabase() {
        return database;
    }

    @Override
    public ConfigurationNode getConfiguration() {
        return config;
    }

    @Override
    public PlatformAdapter getPlatform() {
        return platform;
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
