package me.lorenzo0111.rocketbans.bungee;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.RocketBansProvider;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.bungee.commands.BungeeCommand;
import me.lorenzo0111.rocketbans.bungee.listeners.PlayerListener;
import me.lorenzo0111.rocketbans.bungee.platform.BungeePlatform;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.managers.MuteManager;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import me.lorenzo0111.rocketbans.tasks.ActiveTask;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class RocketBans extends Plugin implements RocketBansPlugin {
    private PlatformAdapter platform;
    private ConfigurationNode config;
    private boolean firstRun = true;
    private SQLHandler database;
    private MuteManager muteManager;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        RocketBansProvider.set(this);

        this.platform = new BungeePlatform(this);

        if (new File(this.getDataFolder(), "config.yml").exists())
            this.firstRun = false;

        // todo: extract config

        this.database = new SQLHandler(this);
        this.muteManager = new MuteManager(this);

        this.reload();
        this.firstRun = false;

        // ******** Commands ********
        BungeeCommand command = new BungeeCommand(this);
        Arrays.asList("rocketbans", "rb", "ban", "tempban", "unban",
                "kick", "mute", "tempmute", "unmute", "history"
        ).forEach(command::register);

        // ******** Listeners ********
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));

        // ******** Tasks ********
        this.getProxy().getScheduler().schedule(this, new ActiveTask(this), 0, 1, TimeUnit.HOURS);
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
        CommandSender logger = this.getProxy().getConsole();
        logger.sendMessage(new TextComponent(StringUtils.color(getMessage("prefix") + message)));
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
