package me.lemurxd.skyblockplugin;

import me.lemurxd.skyblockplugin.Listeners.BlockBreak;
import me.lemurxd.skyblockplugin.Listeners.BlockPlace;
import me.lemurxd.skyblockplugin.commands.SkyBlockPluginCommand;
import me.lemurxd.skyblockplugin.craftings.Generator;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.tasks.PlayerY;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.locks.Condition;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

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

        getCommand("skyblockplugin").setExecutor(new SkyBlockPluginCommand());

    }

    public static Main getInstance() {
        return instance;
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