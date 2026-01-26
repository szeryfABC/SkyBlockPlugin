package me.lemurxd.skyblockplugin.constructors;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkyBlockUser {

    private static final List<SkyBlockUser> skyBlockUsers = new ArrayList<>();

    private static final ItemStack mitycznyDiament = MythicBukkit.inst().getItemManager().getItemStack("mitycznyDiament");

    private final UUID playerUniqueId;
    private List<DropEntry> drops;
    private int dropLevel;
    private long lastOrbUsage;

    public SkyBlockUser(UUID playerUniqueId, List<DropEntry> drops, int dropLevel, long lastOrbUsage) {
        this.playerUniqueId = playerUniqueId;
        this.drops = drops;
        this.dropLevel = dropLevel;
        this.lastOrbUsage = lastOrbUsage;
    }

    public static void createSkyBlockUser(UUID playerUniqueId, List<DropEntry> drops, int dropLevel, long lastOrbUsage) {
        SkyBlockUser user = new SkyBlockUser(playerUniqueId, drops, dropLevel, lastOrbUsage);
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

        switch (level) {
            case 1:
                drops.add(new DropEntry(new ItemStack(Material.STONE), 100, true));
                drops.add(new DropEntry(new ItemStack(Material.COAL), 1, true));
                drops.add(new DropEntry(new ItemStack(Material.COPPER_INGOT), 0.8, true));
                break;
            case 2:
                drops.add(new DropEntry(new ItemStack(Material.STONE), 100, true));
                drops.add(new DropEntry(new ItemStack(Material.COAL), 2, true));
                drops.add(new DropEntry(new ItemStack(Material.IRON_INGOT), 1.5, true));
                drops.add(new DropEntry(new ItemStack(Material.COPPER_INGOT), 0.5, true));
                drops.add(new DropEntry(new ItemStack(Material.GOLD_INGOT), 0.3, true));
                drops.add(new DropEntry(mitycznyDiament, 0.001, true));
                break;
            case 3:
                drops.add(new DropEntry(new ItemStack(Material.STONE), 100, true));
                drops.add(new DropEntry(new ItemStack(Material.COAL), 5, true));
                drops.add(new DropEntry(new ItemStack(Material.IRON_INGOT), 3, true));
                drops.add(new DropEntry(new ItemStack(Material.COPPER_INGOT), 1.5, true));
                drops.add(new DropEntry(new ItemStack(Material.GOLD_INGOT), 0.6, true));
                drops.add(new DropEntry(new ItemStack(Material.DIAMOND), 0.08, true));
                drops.add(new DropEntry(new ItemStack(Material.EMERALD), 0.04, true));
                drops.add(new DropEntry(mitycznyDiament, 0.005, true));
                break;
            case 4:
                drops.add(new DropEntry(new ItemStack(Material.STONE), 100, true));
                drops.add(new DropEntry(new ItemStack(Material.COAL), 6, true));
                drops.add(new DropEntry(new ItemStack(Material.IRON_INGOT), 5, true));
                drops.add(new DropEntry(new ItemStack(Material.COPPER_INGOT), 2.5, true));
                drops.add(new DropEntry(new ItemStack(Material.GOLD_INGOT), 1, true));
                drops.add(new DropEntry(new ItemStack(Material.DIAMOND), 0.3, true));
                drops.add(new DropEntry(new ItemStack(Material.EMERALD), 0.1, true));
                drops.add(new DropEntry(mitycznyDiament, 0.01, true));
                break;
            case 5:
                drops.add(new DropEntry(new ItemStack(Material.STONE), 100, true));
                drops.add(new DropEntry(new ItemStack(Material.COAL), 10, true));
                drops.add(new DropEntry(new ItemStack(Material.IRON_INGOT), 8.5, true));
                drops.add(new DropEntry(new ItemStack(Material.COPPER_INGOT), 5, true));
                drops.add(new DropEntry(new ItemStack(Material.GOLD_INGOT), 3, true));
                drops.add(new DropEntry(new ItemStack(Material.DIAMOND), 0.8, true));
                drops.add(new DropEntry(new ItemStack(Material.EMERALD), 0.5, true));
                drops.add(new DropEntry(new ItemStack(Material.NETHERITE_SCRAP), 0.25, true));
                drops.add(new DropEntry(mitycznyDiament, 0.05, true));
                break;
        }
        return drops;
    }
}
