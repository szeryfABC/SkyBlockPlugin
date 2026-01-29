package me.lemurxd.skyblockplugin.constructors;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.drops.Drop;
import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkyBlockUser {

    private static final List<SkyBlockUser> skyBlockUsers = new ArrayList<>();

    private final UUID playerUniqueId;
    private List<DropEntry> drops;
    private int dropLevel;
    private long lastOrbUsage;
    private boolean magnetEnabled;

    public SkyBlockUser(UUID playerUniqueId, List<DropEntry> drops, int dropLevel, long lastOrbUsage, boolean magnetEnabled) {
        this.playerUniqueId = playerUniqueId;
        this.drops = drops;
        this.dropLevel = dropLevel;
        this.lastOrbUsage = lastOrbUsage;
        this.magnetEnabled = magnetEnabled;
    }

    public static void createSkyBlockUser(UUID playerUniqueId, List<DropEntry> drops, int dropLevel, long lastOrbUsage, boolean magnetEnabled) {
        SkyBlockUser user = new SkyBlockUser(playerUniqueId, drops, dropLevel, lastOrbUsage, magnetEnabled);
        skyBlockUsers.add(user);
    }

    public static SkyBlockUser getSkyBlockUser(UUID playerUniqueId) {
        return skyBlockUsers.stream()
                .filter(user -> user.playerUniqueId.equals(playerUniqueId))
                .findFirst()
                .orElse(null);
    }

    public static List<SkyBlockUser> getSkyBlockUsers() {
        return skyBlockUsers;
    }

    public List<DropEntry> getDrops() { return drops; }
    public int getDropLevel() { return dropLevel; }
    public UUID getPlayerUniqueId() { return playerUniqueId; }
    public long getLastOrbUsage() { return lastOrbUsage; }
    public boolean isMagnetEnabled() { return magnetEnabled; }

    public void setMagnetEnabled(boolean magnetEnabled) {
        this.magnetEnabled = magnetEnabled;
    }

    public void setDropLevel(int level) {
        this.dropLevel = level;
        drops.clear();
        drops = getDropsForLevel(level);
    }

    public void setLastOrbUsage(long newTimeUsage) {
        this.lastOrbUsage = newTimeUsage;
    }

    public static List<DropEntry> getDropsForLevel(int level) {
        List<DropEntry> drops = new ArrayList<>();

        String[] stringDrops = Config.DROP_DROPS.getStringList().get(level - 1).split("<n>");
        for (String s : stringDrops) {
            String[] drop = s.split(":");
            if (MythicBukkit.inst().getItemManager().getItemStack(drop[0]) != null) {
                drops.add(new DropEntry(MythicBukkit.inst().getItemManager().getItemStack(drop[0]), Double.parseDouble(drop[1]), true));
            } else {
                drops.add(new DropEntry(new ItemStack(Material.getMaterial(drop[0])), Double.parseDouble(drop[1]), true));
            }
        }
        return drops;
    }
}
