package me.lorenzo0111.rocketbans.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.Table;
import org.spongepowered.configurate.ConfigurationNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLHandler {
    private final RocketBansPlugin plugin;
    private final Executor executor;
    private HikariDataSource dataSource;

    public SQLHandler(RocketBansPlugin plugin) {
        this.plugin = plugin;
        this.executor = plugin.getPlatform()::async;
    }

    public void init() throws SQLException {
        ConfigurationNode cs = plugin.getConfiguration().node("mysql");
        Objects.requireNonNull(cs, "Unable to find the following key: mysql");
        HikariConfig hikari = new HikariConfig();

        hikari.setJdbcUrl("jdbc:mysql://" + cs.node("host").getString() + ":" + cs.node("port").getInt() + "/" + cs.node("database").getString(""));
        hikari.setUsername(cs.node("username").getString());
        hikari.setPassword(cs.node("password").getString());
        hikari.setConnectionTimeout(10000);
        hikari.setLeakDetectionThreshold(10000);
        hikari.setMaximumPoolSize(10);
        hikari.setMaxLifetime(60000);
        hikari.setPoolName("RocketBansPool");
        hikari.addDataSourceProperty("useSSL", cs.node("ssl").getBoolean());

        this.dataSource = new HikariDataSource(hikari);
        this.createTables();
    }

    private void createTables() throws SQLException {
        Connection connection = this.getConnection();

        Statement statement = connection.createStatement();
        String expiringTable = "CREATE TABLE IF NOT EXISTS `%s` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`reason` TEXT NOT NULL," +
                "`executor` VARCHAR(36) NOT NULL," +
                "`date` DATETIME NOT NULL," +
                "`expires` DATETIME," +
                "`active` BOOLEAN NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ");";

        statement.executeUpdate(String.format(expiringTable, Table.BANS));
        statement.executeUpdate(String.format(expiringTable, Table.MUTES));
        statement.executeUpdate(String.format(expiringTable, Table.WARNS));

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `kicks` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`reason` TEXT NOT NULL," +
                "`executor` VARCHAR(36) NOT NULL," +
                "`date` DATETIME NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ");");

        connection.close();
    }

    public <T extends HistoryRecord> CompletableFuture<List<T>> get(Class<T> type, UUID uuid, boolean onlyActive) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> results = new ArrayList<>();
            Table table = Table.fromClass(type);
            if (table == null) return results;

            try (Connection connection = this.getConnection()) {
                String query = "SELECT * FROM `%s` WHERE uuid = ?%s ORDER BY date DESC;";

                PreparedStatement statement = connection.prepareStatement(String.format(query, table, onlyActive ? " AND active = true" : ""));
                statement.setString(1, uuid.toString());

                ResultSet set = statement.executeQuery();
                results.addAll(this.deserialize(type, set));

                set.close();
                statement.close();
            } catch (SQLException e) {
                plugin.getPlatform().logException(e);
            }

            return results;
        }, this.executor);
    }

    public <T extends HistoryRecord> CompletableFuture<List<T>> getActive(Class<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> results = new ArrayList<>();
            Table table = Table.fromClass(type);
            if (table == null) return results;

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + table + "` WHERE active = true ORDER BY date DESC;");
                ResultSet set = statement.executeQuery();

                results.addAll(this.deserialize(type, set));

                set.close();
                statement.close();
            } catch (SQLException e) {
                plugin.getPlatform().logException(e);
            }

            return results;
        }, this.executor);
    }

    public <T extends HistoryRecord> void expireAll(Class<T> type, UUID uuid) {
        CompletableFuture.runAsync(() -> {
            Table table = Table.fromClass(type);
            if (table == null) return;

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE `" + table + "` SET active = false WHERE uuid = ?;");
                statement.setString(1, uuid.toString());

                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                plugin.getPlatform().logException(e);
            }
        }, this.executor);
    }

    public <T extends HistoryRecord> CompletableFuture<Integer> add(T item) {
        return CompletableFuture.supplyAsync(() -> {
            Table table = Table.fromClass(item.getClass());
            if (table == null) return -1;

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        String.format("INSERT INTO `" + table + "` (%s) VALUES (%s);",
                                "uuid, reason, executor, date" + (table.isExpiring() ? ", expires, active" : ""),
                                "?, ?, ?, ?" + (table.isExpiring() ? ", ?, ?" : "")
                        ),
                        Statement.RETURN_GENERATED_KEYS
                );

                statement.setString(1, item.uuid().toString());
                statement.setString(2, item.reason());
                statement.setString(3, item.executor().toString());
                statement.setTimestamp(4, item.date());
                if (item instanceof ExpiringRecord expiring) {
                    statement.setTimestamp(5, expiring.expires());
                    statement.setBoolean(6, expiring.active());
                }

                statement.executeUpdate();

                ResultSet set = statement.getGeneratedKeys();
                int res = -1;

                if (set.next()) {
                    res = set.getInt(1);
                }

                set.close();
                statement.close();

                return res;
            } catch (SQLException e) {
                plugin.getPlatform().logException(e);
            }

            return -1;
        }, this.executor);
    }

    public <T extends HistoryRecord> void expireSingle(Class<T> type, int id) {
        CompletableFuture.runAsync(() -> {
            Table table = Table.fromClass(type);
            if (table == null) return;

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE `" + table + "` SET active = false WHERE id = ?;");
                statement.setInt(1, id);

                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getPlatform().logException(e);
            }
        }, this.executor);
    }

    public <T extends HistoryRecord> void delete(Class<T> type, int id) {
        CompletableFuture.runAsync(() -> {
            Table table = Table.fromClass(type);
            if (table == null) return;

            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + table + "` WHERE id = ?;");
                statement.setInt(1, id);

                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getPlatform().logException(e);
            }
        }, this.executor);
    }

    private <T extends HistoryRecord> List<T> deserialize(Class<T> type, ResultSet set) throws SQLException {
        List<T> results = new ArrayList<>();
        Table table = Table.fromClass(type);
        if (table == null) return results;

        while (set.next()) {
            results.add(table.create(set.getInt("id"),
                    UUID.fromString(set.getString("uuid")),
                    set.getString("reason"),
                    UUID.fromString(set.getString("executor")),
                    set.getTimestamp("date"),
                    table.isExpiring() ? set.getTimestamp("expires") : null,
                    table.isExpiring() ? set.getBoolean("active") : null));
        }

        return results;
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

    public Executor getExecutor() {
        return executor;
    }
}
