package me.lorenzo0111.rocketbans;

import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.tasks.ActiveTask;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.UUID;

public final class RocketBans extends JavaPlugin {
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static RocketBans instance;
    private boolean firstRun = true;
    private SQLHandler database;

    @Override
    public void onEnable() {
        instance = this;

        if (new File(this.getDataFolder(), "config.yml").exists())
            this.firstRun = false;

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.database = new SQLHandler(this);

        this.reload();
        this.firstRun = false;

        this.getCommand("rocketbans").setExecutor(new RocketBansCommand(this));

        // ******** Listeners ********


        // ******** Tasks ********
        new ActiveTask(this);
    }


    @Override
    public void onDisable() {
        try {
            this.getDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
            this.log("&cAn error occurred while disabling the plugin.");
            this.log("&cError: " + e.getMessage());
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
}
