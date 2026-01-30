package me.lemurxd.skyblockplugin;

import me.lemurxd.skyblockplugin.commands.DropCommand;
import me.lemurxd.skyblockplugin.commands.OrbCommand;
import me.lemurxd.skyblockplugin.commands.SkyBlockPluginCommand;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.craftings.Generator;
import me.lemurxd.skyblockplugin.database.SkyBlockUserDatabase;
import me.lemurxd.skyblockplugin.database.StoneGeneratorDatabase;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.listeners.*;
import me.lemurxd.skyblockplugin.tasks.DataBaseTask;
import me.lemurxd.skyblockplugin.tasks.PlayerY;
import me.pikamug.quests.Quests;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    private static Economy economy;

    private static Main instance;
    private static StoneGeneratorDatabase generatorDatabase;
    private static SkyBlockUserDatabase userDatabase;
    private Connection connection;

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        try {
            File databaseFile = new File(getDataFolder(), "baza.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        generatorDatabase = new StoneGeneratorDatabase(connection);
        userDatabase = new SkyBlockUserDatabase(connection);

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
        Bukkit.getPluginManager().registerEvents(new Inventory(), getInstance());
        Bukkit.getPluginManager().registerEvents(new IslandDelete(), getInstance());
        Bukkit.getPluginManager().registerEvents(new StoneGeneratorProtection(), getInstance());
        Bukkit.getPluginManager().registerEvents(new StoneGeneratorLoad(), getInstance());


        if (getServer().getPluginManager().getPlugin("Quests") instanceof Quests) {
            getLogger().info("Znaleziono Quests! Rejestrowanie celu MythicMobs...");

            Quests quests = (Quests) getServer().getPluginManager().getPlugin("Quests");

            MythicMobsKillObjective mmObjective = new MythicMobsKillObjective();

            getServer().getPluginManager().registerEvents(mmObjective, getInstance());

            try {
                quests.getCustomObjectives().add(mmObjective);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        new DataBaseTask().runTaskTimer(this, 12000L, 12000L);


        getCommand("skyblockplugin").setExecutor(new SkyBlockPluginCommand());
        getCommand("drop").setExecutor(new DropCommand());
        getCommand("orb").setExecutor(new OrbCommand());

        setupEconomy();
    }

    @Override
    public void onDisable() {
        if (generatorDatabase != null) {
            getLogger().info("Zapisywanie stoniarek...");
            generatorDatabase.saveGenerators(StoneGenerator.getMap());
        }

        if (userDatabase != null) {
            getLogger().info("Zapisywanie użytkowników...");
            for (SkyBlockUser user : SkyBlockUser.getSkyBlockUsers()) {
                userDatabase.saveUser(user);
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        saveMainYML();
        getLogger().info("Pomyślnie zapisano dane i wyłączono plugin.");
    }

    public static Main getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static StoneGeneratorDatabase getDatabase() {
        return generatorDatabase;
    }

    public static SkyBlockUserDatabase getUserDatabase() {
        return userDatabase;
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