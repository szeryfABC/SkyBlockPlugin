package me.lemurxd.skyblockplugin.database;

import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import world.bentobox.bentobox.database.objects.Island;

import java.sql.*;
import java.util.*;

public class StoneGeneratorDatabase {

    private final Connection connection;


    public StoneGeneratorDatabase(Connection connection) {
        this.connection = connection;
        createTable();
    }

    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS stone_generators (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "island_id VARCHAR(36), " +
                "world_name VARCHAR(64) NOT NULL, " +
                "x DOUBLE NOT NULL, " +
                "y DOUBLE NOT NULL, " +
                "z DOUBLE NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveGenerators(Map<Optional<Island>, List<StoneGenerator>> map) {
        String deleteSql = "DELETE FROM stone_generators WHERE island_id = ?";
        String insertSql = "INSERT INTO stone_generators (island_id, world_name, x, y, z) VALUES (?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                for (Map.Entry<Optional<Island>, List<StoneGenerator>> entry : map.entrySet()) {
                    Optional<Island> islandOpt = entry.getKey();
                    List<StoneGenerator> generators = entry.getValue();

                    String islandIdStr = islandOpt.map(island -> island.getUniqueId()).orElse(null);

                    if (islandIdStr != null) {
                        deleteStmt.setString(1, islandIdStr);
                        deleteStmt.executeUpdate();
                    } else {
                        Statement stmt = connection.createStatement();
                        stmt.executeUpdate("DELETE FROM stone_generators WHERE island_id IS NULL");
                        stmt.close();
                    }

                    for (StoneGenerator gen : generators) {
                        insertStmt.setString(1, islandIdStr);

                        Location loc = gen.getLocation();
                        insertStmt.setString(2, loc.getWorld().getName());
                        insertStmt.setDouble(3, loc.getX());
                        insertStmt.setDouble(4, loc.getY());
                        insertStmt.setDouble(5, loc.getZ());

                        insertStmt.addBatch();
                    }
                }

                insertStmt.executeBatch();

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

    public List<StoneGenerator> loadGeneratorsForIsland(Island island) {
        List<StoneGenerator> list = new ArrayList<>();
        String targetId = island.getUniqueId();

        String sql = "SELECT * FROM stone_generators WHERE island_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, targetId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String worldName = rs.getString("world_name");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    Location location = new Location(world, x, y, z);

                    list.add(new StoneGenerator(Optional.of(island), location));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<Optional<Island>, List<StoneGenerator>> loadGenerators(IslandProvider islandProvider) {
        Map<Optional<Island>, List<StoneGenerator>> result = new HashMap<>();
        String sql = "SELECT * FROM stone_generators";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                String islandId = rs.getString("island_id");
                String worldName = rs.getString("world_name");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");

                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                Location location = new Location(world, x, y, z);

                Optional<Island> islandOpt = Optional.empty();
                if (islandId != null) {
                    Island foundIsland = islandProvider.findIslandById(islandId);
                    islandOpt = Optional.ofNullable(foundIsland);
                }

                StoneGenerator generator = new StoneGenerator(islandOpt, location);

                result.computeIfAbsent(islandOpt, k -> new ArrayList<>()).add(generator);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public interface IslandProvider {
        Island findIslandById(String id);
    }
}

