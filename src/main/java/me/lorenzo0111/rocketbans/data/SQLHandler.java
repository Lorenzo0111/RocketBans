package me.lorenzo0111.rocketbans.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lorenzo0111.rocketbans.RocketBans;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.Executor;

public class SQLHandler {
    private final RocketBans plugin;
    private final Executor executor;
    private HikariDataSource dataSource;

    public SQLHandler(RocketBans plugin) {
        this.plugin = plugin;
        this.executor = (cmd) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, cmd);
    }

    public void init() throws SQLException {
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("mysql");
        Objects.requireNonNull(cs, "Unable to find the following key: mysql");
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + cs.getString("host") + ":" + cs.getString("port") + "/" + cs.getString("database"));
        config.setUsername(cs.getString("username"));
        config.setPassword(cs.getString("password"));
        config.setConnectionTimeout(10000);
        config.setLeakDetectionThreshold(10000);
        config.setMaximumPoolSize(10);
        config.setMaxLifetime(60000);
        config.setPoolName("RocketBansPool");
        config.addDataSourceProperty("useSSL", cs.getBoolean("ssl"));

        this.dataSource = new HikariDataSource(config);
        this.createTables();
    }

    private void createTables() throws SQLException {
        Connection connection = this.getConnection();

        Statement statement = connection.createStatement();


        connection.close();
    }

    public void close() {
        try {
            if (this.isOpen()) {
                this.dataSource.close();
            }
        } catch (Exception ignored) {}

        this.dataSource = null;
    }

    public boolean isOpen() {
        return this.dataSource != null;
    }

    private Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool.");
        }

        return connection;
    }
}
