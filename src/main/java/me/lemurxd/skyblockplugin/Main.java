package me.lemurxd.skyblockplugin;

import me.lemurxd.skyblockplugin.Listeners.BlockBreak;
import me.lemurxd.skyblockplugin.Listeners.BlockPlace;
import me.lemurxd.skyblockplugin.Listeners.PlayerJoin;
import me.lemurxd.skyblockplugin.commands.SkyBlockPluginCommand;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.craftings.Generator;
import me.lemurxd.skyblockplugin.database.StoneGeneratorDatabase;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.tasks.PlayerY;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    private static Main instance;
    private static StoneGeneratorDatabase database;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        Connection connection = null;
        try {
            File databaseFile = new File(getDataFolder(), "baza.db");

            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());

        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        database = new StoneGeneratorDatabase(connection);

        if (!configManager()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (Config.SAFE_SPAWN_ENABLED.getBoolean()) {
            PlayerY.runPlayerYRespawnTimer();
        }

        Bukkit.addRecipe(Generator.getRecipe());


        Bukkit.getPluginManager().registerEvents(new BlockBreak(), getInstance());
        Bukkit.getPluginManager().registerEvents(new BlockPlace(), getInstance());
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(), getInstance());

        getCommand("skyblockplugin").setExecutor(new SkyBlockPluginCommand());

    }

    @Override
    public void onDisable() {
        if (database != null) {
            getLogger().info("Trwa zapisywanie stoniarek do bazy danych...");

            database.saveGenerators(StoneGenerator.getMap());

            database.closeConnection();
        }

        saveMainYML();

        getLogger().info("Pomyślnie zapisano dane i wyłączono plugin.");
    }

    public static Main getInstance() {
        return instance;
    }

    public static StoneGeneratorDatabase getDatabase() {
        return database;
    }

    public void saveMainYML() {
        Config.save(new File(this.getDataFolder(), "SkyBlockConfig.yml"));
    }

    private boolean configManager() {
        File f = new File(this.getDataFolder(), "SkyBlockConfig.yml");
        try {
            Config.load(f);
        } catch (Exception e) {
            sendError("Cannot load main config", e);
            return false;
        }
        return true;
    }

    public static void sendError(String message, Exception e) {
        System.err.println(message);
        System.err.println(e.toString());
    }
}