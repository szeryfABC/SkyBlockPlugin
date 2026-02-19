package me.lemurxd.skyblockplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin, String host, int port, String database, String user, String password) {
        this.plugin = plugin;
        try {
            initDataSource(host, port, database, user, password);
            createTable();
            plugin.getLogger().info("Pomyslnie polaczono z baza danych!");
        } catch (Exception e) {
            plugin.getLogger().warning("--------------------------------------------------");
            plugin.getLogger().warning("NIE UDALO SIE POLACZYC Z BAZA DANYCH!");
            plugin.getLogger().warning("Plugin bedzie dzialal, ale zapis ekwipunku jest WYLACZONY.");
            plugin.getLogger().warning("--------------------------------------------------");
            this.dataSource = null;
        }
    }

    private void initDataSource(String host, int port, String database, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_inventories (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "contents LONGTEXT, " +
                "armor LONGTEXT, " +
                "offhand LONGTEXT" +
                ");";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public void savePlayerInventory(Player player) {
        if (this.dataSource == null) return;

        UUID uuid = player.getUniqueId();
        String contentsBase64 = itemStackArrayToBase64(player.getInventory().getContents());
        String armorBase64 = itemStackArrayToBase64(player.getInventory().getArmorContents());

        String offhandBase64 = itemStackArrayToBase64(new ItemStack[]{player.getInventory().getItemInOffHand()});

        CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO player_inventories (uuid, contents, armor, offhand) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE contents=?, armor=?, offhand=?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, uuid.toString());
                ps.setString(2, contentsBase64);
                ps.setString(3, armorBase64);
                ps.setString(4, offhandBase64);

                ps.setString(5, contentsBase64);
                ps.setString(6, armorBase64);
                ps.setString(7, offhandBase64);

                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas zapisywania ekwipunku dla " + player.getName());
                e.printStackTrace();
            }
        });
    }

    public void loadPlayerInventory(Player player) {
        if (this.dataSource == null) return;

        UUID uuid = player.getUniqueId();

        CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT contents, armor, offhand FROM player_inventories WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new String[]{
                                rs.getString("contents"),
                                rs.getString("armor"),
                                rs.getString("offhand")
                        };
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas wczytywania ekwipunku dla " + player.getName());
                e.printStackTrace();
            }
            return null;
        }).thenAccept(data -> {
            if (data == null) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                try {
                    ItemStack[] contents = itemStackArrayFromBase64(data[0]);
                    ItemStack[] armor = itemStackArrayFromBase64(data[1]);
                    ItemStack[] offhandArray = itemStackArrayFromBase64(data[2]);
                    ItemStack offhand = (offhandArray != null && offhandArray.length > 0) ? offhandArray[0] : new ItemStack(Material.AIR);

                    player.getInventory().clear();

                    player.getInventory().setContents(contents);
                    player.getInventory().setArmorContents(armor);
                    player.getInventory().setItemInOffHand(offhand);

                    player.updateInventory();

                    CompletableFuture.runAsync(() -> {
                        String deleteSql = "DELETE FROM player_inventories WHERE uuid = ?";
                        try (Connection conn = dataSource.getConnection();
                             PreparedStatement ps = conn.prepareStatement(deleteSql)) {

                            ps.setString(1, uuid.toString());
                            ps.executeUpdate();

                        } catch (SQLException e) {
                            plugin.getLogger().severe("Błąd podczas usuwania ekwipunku z bazy dla " + player.getName());
                            e.printStackTrace();
                        }
                    });

                } catch (IOException e) {
                    plugin.getLogger().severe("Błąd deserializacji ekwipunku dla " + player.getName());
                    e.printStackTrace();
                }
            });
        });
    }

    private String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Nie udało się zapisać przedmiotów.", e);
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        if (data == null || data.isEmpty()) return new ItemStack[0];

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Nie udało się odczytać klasy przedmiotu.", e);
        }
    }
}
