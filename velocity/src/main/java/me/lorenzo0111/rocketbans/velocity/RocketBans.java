package me.lorenzo0111.rocketbans.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.managers.MuteManager;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import me.lorenzo0111.rocketbans.tasks.ActiveTask;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.velocity.commands.VelocityCommand;
import me.lorenzo0111.rocketbans.velocity.listeners.ChannelListener;
import me.lorenzo0111.rocketbans.velocity.listeners.PlayerListener;
import me.lorenzo0111.rocketbans.managers.BanManager;
import me.lorenzo0111.rocketbans.velocity.platform.VelocityPlatform;
import net.kyori.adventure.text.Component;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Plugin(id = "rocketbans",
        name = "RocketBans",
        version = "@version@",
        authors = {"Lorenzo0111"})
public final class RocketBans implements RocketBansPlugin {
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("rocketbans:sync");

    private final Logger logger;
    private final Path path;
    private final ProxyServer server;
    private final Metrics.Factory metricsFactory;

    private PlatformAdapter platform;
    private ConfigurationNode config;
    private boolean firstRun = true;
    private SQLHandler database;
    private MuteManager muteManager;
    private BanManager banManager;

    @Inject
    public RocketBans(Logger logger, @DataDirectory Path path, ProxyServer server, Metrics.Factory metricsFactory) {
        this.logger = logger;
        this.path = path;
        this.server = server;
        this.metricsFactory = metricsFactory;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        RocketBansProvider.set(this);

        this.platform = new VelocityPlatform(this);

        if (new File(path.toFile(), "config.yml").exists())
            this.firstRun = false;

        try {
            this.extractConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        metricsFactory.make(this, 22814);

        this.database = new SQLHandler(this);
        this.muteManager = new MuteManager(this);
        this.banManager = new BanManager(this);

        this.reload();
        this.firstRun = false;

        // ******** Commands ********
        CommandMeta commandMeta = server.getCommandManager()
                .metaBuilder("rocketbans")
                .aliases("rb", "ban", "tempban", "unban",
                        "kick", "mute", "tempmute", "unmute", "history")
                .plugin(this)
                .build();
        server.getCommandManager().register(commandMeta, new VelocityCommand(this));

        // ******** Listeners ********
        server.getEventManager().register(this, new PlayerListener(this));
        server.getEventManager().register(this, new ChannelListener(this));

        // ******** Tasks ********
        server.getScheduler().buildTask(this, new ActiveTask(this))
                .repeat(1, TimeUnit.HOURS)
                .schedule();
        server.getScheduler().buildTask(this, banManager::reload)
                .repeat(1, TimeUnit.HOURS)
                .schedule();

        server.getChannelRegistrar().register(IDENTIFIER);
    }

    public void log(String message) {
        ConsoleCommandSource logger = server.getConsoleCommandSource();
        logger.sendMessage(Component.text(StringUtils.color(getMessage("prefix") + message)));
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
                    .file(new File(path.toFile(), "config.yml"))
                    .build()
                    .load();
        } catch (ConfigurateException e) {
            platform.logException(e);
        }

        this.log("&c&m---------------------------------------------------");
        this.log("             &c&lRocket&e&lBans &7v" + getVersion());
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
        return "@version@";
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

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
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
        if (!Files.exists(path)) {
            logger.info("Created config folder: {}", Files.createDirectories(path));
        }

        File configFile = new File(path.toFile(), "config.yml");

        if (!configFile.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Objects.requireNonNull(in);

                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                platform.logException(e);
            }
        }
    }
}
