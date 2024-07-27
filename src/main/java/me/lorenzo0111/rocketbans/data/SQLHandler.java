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
                "`expires` DATETIME," +
                "`active` BOOLEAN NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ");");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `kicks` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`reason` TEXT NOT NULL," +
                "`executor` VARCHAR(36) NOT NULL," +
                "`date` DATETIME NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ");");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `mutes` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`reason` TEXT NOT NULL," +
                "`executor` VARCHAR(36) NOT NULL," +
                "`date` DATETIME NOT NULL," +
                "`expires` DATETIME," +
                "`active` BOOLEAN NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ");");

        connection.close();
    }

    public CompletableFuture<List<Ban>> getBans(UUID uuid, boolean onlyActive) {
        return CompletableFuture.supplyAsync(() -> {
            List<Ban> bans = new ArrayList<>();

            try (Connection connection = this.getConnection()) {
                StringBuilder query = new StringBuilder("SELECT * FROM `bans` WHERE uuid = ?");
                if (onlyActive) {
                    query.append(" AND active = true");
                }

                query.append(";");

                PreparedStatement statement = connection.prepareStatement(query.toString());
                statement.setString(1, uuid.toString());

                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    bans.add(new Ban(
                            set.getInt("id"),
                            UUID.fromString(set.getString("uuid")),
                            set.getString("reason"),
                            UUID.fromString(set.getString("executor")),
                            set.getTimestamp("date"),
                            set.getTimestamp("expires"),
                            set.getBoolean("active")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return bans;
        }, this.executor);
    }


    public CompletableFuture<List<Ban>> getActiveBans() {
        return CompletableFuture.supplyAsync(() -> {
            List<Ban> bans = new ArrayList<>();

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `bans` WHERE active = true;");
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    bans.add(new Ban(
                            set.getInt("id"),
                            UUID.fromString(set.getString("uuid")),
                            set.getString("reason"),
                            UUID.fromString(set.getString("executor")),
                            set.getTimestamp("date"),
                            set.getTimestamp("expires"),
                            set.getBoolean("active")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return bans;
        }, this.executor);
    }

    public CompletableFuture<List<Mute>> getActiveMutes() {
        return CompletableFuture.supplyAsync(() -> {
            List<Mute> bans = new ArrayList<>();

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `mutes` WHERE active = true;");
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    bans.add(new Mute(
                            set.getInt("id"),
                            UUID.fromString(set.getString("uuid")),
                            set.getString("reason"),
                            UUID.fromString(set.getString("executor")),
                            set.getTimestamp("date"),
                            set.getTimestamp("expires"),
                            set.getBoolean("active")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return bans;
        }, this.executor);
    }

    public void unban(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE `bans` SET active = false WHERE uuid = ?;");
                statement.setString(1, uuid.toString());

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, this.executor);
    }

    public void addBan(Ban ban) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO `bans` (uuid, reason, executor, date, expires, active) VALUES (?, ?, ?, ?, ?, ?);");
                statement.setString(1, ban.uuid().toString());
                statement.setString(2, ban.reason());
                statement.setString(3, ban.executor().toString());
                statement.setTimestamp(4, ban.date());
                statement.setTimestamp(5, ban.expires());
                statement.setBoolean(6, ban.active());

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, this.executor);
    }

    public void expireBan(int id) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE `bans` SET active = false WHERE id = ?;");
                statement.setInt(1, id);

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, this.executor);
    }

    public void addKick(Kick kick) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO `kicks` (uuid, reason, executor, date) VALUES (?, ?, ?, ?);");
                statement.setString(1, kick.uuid().toString());
                statement.setString(2, kick.reason());
                statement.setString(3, kick.executor().toString());
                statement.setTimestamp(4, kick.date());

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, this.executor);
    }

    public CompletableFuture<Integer> addMute(Mute mute) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO `mutes` (uuid, reason, executor, date, expires, active) VALUES (?, ?, ?, ?, ?, ?);",
                        Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, mute.uuid().toString());
                statement.setString(2, mute.reason());
                statement.setString(3, mute.executor().toString());
                statement.setTimestamp(4, mute.date());
                statement.setTimestamp(5, mute.expires());
                statement.setBoolean(6, mute.active());

                return statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return -1;
        }, this.executor);
    }

    public void expireMute(int id) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE `mutes` SET active = false WHERE id = ?;");
                statement.setInt(1, id);

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
