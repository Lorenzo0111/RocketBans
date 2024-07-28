package me.lorenzo0111.rocketbans.bungee;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.bungee.commands.BungeeCommand;
import me.lorenzo0111.rocketbans.bungee.listeners.ChannelListener;
import me.lorenzo0111.rocketbans.bungee.listeners.PlayerListener;
import me.lorenzo0111.rocketbans.managers.BanManager;
import me.lorenzo0111.rocketbans.bungee.platform.BungeePlatform;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.managers.MuteManager;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import me.lorenzo0111.rocketbans.tasks.ActiveTask;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private BanManager banManager;

    @Override
    public void onEnable() {
        RocketBansProvider.set(this);

        this.platform = new BungeePlatform(this);

        if (new File(this.getDataFolder(), "config.yml").exists())
            this.firstRun = false;

        try {
            this.extractConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new Metrics(this, 22813);

        this.database = new SQLHandler(this);
        this.muteManager = new MuteManager(this);
        this.banManager = new BanManager(this);

        this.reload();
        this.firstRun = false;

        // ******** Commands ********
        BungeeCommand command = new BungeeCommand(this);
        Arrays.asList("rocketbans", "rb", "ban", "tempban", "unban",
                "kick", "mute", "tempmute", "unmute", "history"
        ).forEach(command::register);

        // ******** Listeners ********
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        this.getProxy().getPluginManager().registerListener(this, new ChannelListener(this));

        // ******** Tasks ********
        this.getProxy().getScheduler().schedule(this, new ActiveTask(this), 0, 1, TimeUnit.HOURS);
        this.getProxy().getScheduler().schedule(this, banManager::reload, 0, 1, TimeUnit.HOURS);

        this.getProxy().registerChannel("rocketbans:sync");
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
                this.getConfiguration().node((Object[]) (messagesSection ? "messages." + path : path).split("\\.")).getString("&cUnable to find the following key: &7" + path + "&c.")
        );
    }

    public List<String> getMessages(String path, boolean messagesSection) {
        try {
            return StringUtils.color(
                    this.getConfiguration()
                            .node((Object[]) (messagesSection ? "messages." + path : path).split("\\."))
                            .getList(String.class, new ArrayList<>())
            );
        } catch (SerializationException e) {
            return new ArrayList<>();
        }
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
                this.getConfiguration().node((Object[]) ("messages." + path).split("\\.")).getString("&cUnable to find the following key: &7" + path + "&c.")
        );
    }

    @Override
    public void reload() {
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
                this.banManager.reload();
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

    public BanManager getBanManager() {
        return banManager;
    }

    @Override
    public <T extends HistoryRecord> void punish(T item) {
        database.add(item);

        if (item instanceof Mute)
            muteManager.addMute((Mute) item);

        if (item instanceof Ban ban)
            platform.ban(platform.getPlayer(ban.uuid()), ban.reason(), ban.expires(), ban.executor());
    }

    @Override
    public <T extends HistoryRecord> void expire(Class<T> type, UUID uuid) {
        database.expireAll(type, uuid);

        if (type.equals(Mute.class))
            muteManager.removeMutes(uuid);

        if (type.equals(Ban.class))
            platform.unban(platform.getPlayer(uuid));
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

    private void extractConfig() throws IOException {
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            InputStream in = getResourceAsStream("config.yml");
            in.transferTo(outputStream);
            in.close();
        }
    }

}
