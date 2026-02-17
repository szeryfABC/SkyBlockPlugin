package me.lemurxd.skyblockplugin.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaseFile {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    // Cache w pamięci RAM
    private final Map<String, IslandData> cache = new HashMap<>();

    public BaseFile(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");

        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void setNether(String uuid, boolean allowed) {
        getData(uuid).nether = allowed;
        saveAndSet(uuid, "nether", allowed);
    }

    public boolean isNetherUnlocked(String uuid) {
        return getData(uuid).nether;
    }

    public void setEnd(String uuid, boolean allowed) {
        getData(uuid).end = allowed;
        saveAndSet(uuid, "end", allowed);
    }

    public boolean isEndUnlocked(String uuid) {
        return getData(uuid).end;
    }

    public void setKowal(String uuid, boolean allowed) {
        getData(uuid).kowal = allowed;
        saveAndSet(uuid, "kowal", allowed);
    }

    public boolean isKowalUnlocked(String uuid) {
        return getData(uuid).kowal;
    }

    public void setHordy(String uuid, boolean allowed) {
        getData(uuid).hordy = allowed;
        saveAndSet(uuid, "hordy", allowed);
    }

    public boolean isHordyUnlocked(String uuid) {
        return getData(uuid).hordy;
    }

    public void setDungs(String uuid, boolean allowed) {
        getData(uuid).dungs = allowed;
        saveAndSet(uuid, "dungs", allowed);
    }

    public boolean isDungsUnlocked(String uuid) {
        return getData(uuid).dungs;
    }

    private IslandData getData(String uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        boolean nether = config.getBoolean("islands." + uuid + ".nether", false);
        boolean end = config.getBoolean("islands." + uuid + ".end", false);
        boolean kowal = config.getBoolean("islands." + uuid + ".kowal", false);
        boolean hordy = config.getBoolean("islands." + uuid + ".hordy", false);
        boolean dungs = config.getBoolean("islands." + uuid + ".dungs", false);

        IslandData data = new IslandData(nether, end, kowal, hordy, dungs);
        cache.put(uuid, data);

        return data;
    }

    private void saveAndSet(String uuid, String key, boolean value) {
        config.set("islands." + uuid + "." + key, value);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Blad zapisu data.yml!");
            e.printStackTrace();
        }
    }

    private static class IslandData {
        boolean nether;
        boolean end;
        boolean kowal;
        boolean hordy;
        boolean dungs;

        public IslandData(boolean nether, boolean end, boolean kowal, boolean hordy, boolean dungs) {
            this.nether = nether;
            this.end = end;
            this.kowal = kowal;
            this.hordy = hordy;
            this.dungs = dungs;
        }
    }
}