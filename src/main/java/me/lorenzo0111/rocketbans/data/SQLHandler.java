package me.lorenzo0111.rocketbans.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lorenzo0111.rocketbans.RocketBans;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `bans` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`reason` TEXT NOT NULL," +
                "`executor` VARCHAR(36) NOT NULL," +
                "`date` DATETIME NOT NULL," +
                "`expires` DATETIME NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ");");

        connection.close();
    }

    public CompletableFuture<List<Ban>> getBans(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Ban> bans = new ArrayList<>();

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `bans` WHERE uuid = ?;");
                statement.setString(1, uuid.toString());

                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    bans.add(new Ban(
                            set.getInt("id"),
                            UUID.fromString(set.getString("uuid")),
                            set.getString("reason"),
                            UUID.fromString(set.getString("executor")),
                            set.getTimestamp("date"),
                            set.getTimestamp("expires")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return bans;
        }, this.executor);
    }

    public void addBan(Ban ban) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO `bans` (uuid, reason, executor, date, expires) VALUES (?, ?, ?, ?, ?);");
                statement.setString(1, ban.uuid().toString());
                statement.setString(2, ban.reason());
                statement.setString(3, ban.executor().toString());
                statement.setTimestamp(4, ban.date());
                statement.setTimestamp(5, ban.expires());

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, this.executor);
    }

    public void close() {
        try {
            if (this.isOpen()) {
                this.dataSource.close();
            }
        } catch (Exception ignored) {
        }

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
