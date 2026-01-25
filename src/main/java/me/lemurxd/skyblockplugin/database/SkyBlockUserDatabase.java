package me.lemurxd.skyblockplugin.database;

import me.lemurxd.skyblockplugin.constructors.DropEntry;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.tasks.DataBaseTask;
import me.lemurxd.skyblockplugin.utils.DropSerializer;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class SkyBlockUserDatabase {

    private final Connection connection;

    public SkyBlockUserDatabase(Connection connection) {
        this.connection = connection;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS skyblock_users (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "drop_level INTEGER NOT NULL, " +
                "drops_data TEXT NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(SkyBlockUser user) {
        String sql = "INSERT OR REPLACE INTO skyblock_users (uuid, drop_level, drops_data) VALUES (?, ?, ?)";

        String serializedDrops = DropSerializer.serialize(user.getDrops());

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getPlayerUniqueId().toString());
            stmt.setInt(2, user.getDropLevel());
            stmt.setString(3, serializedDrops);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SkyBlockUser loadUser(UUID uuid) {
        String sql = "SELECT * FROM skyblock_users WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int level = rs.getInt("drop_level");
                    String dropsData = rs.getString("drops_data");

                    List<DropEntry> drops = DropSerializer.deserialize(dropsData);

                    return new SkyBlockUser(uuid, drops, level);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveUsersBatch(List<DataBaseTask.UserSnapshot> snapshots) {
        String sql = "INSERT OR REPLACE INTO skyblock_users (uuid, drop_level, drops_data) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (DataBaseTask.UserSnapshot snap : snapshots) {
                    stmt.setString(1, snap.uuid());
                    stmt.setInt(2, snap.level());
                    stmt.setString(3, snap.data());
                    stmt.addBatch();
                }

                stmt.executeBatch();
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
