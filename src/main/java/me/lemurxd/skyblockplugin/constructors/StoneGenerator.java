package me.lemurxd.skyblockplugin.constructors;

import me.lemurxd.skyblockplugin.utils.NBTUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;

public class StoneGenerator {

    private static HashMap<Optional<Island>, List<StoneGenerator>> generatorMap = new HashMap<>();

    private Location location;
    private Optional<Island> island;
    
    public StoneGenerator(Optional<Island> island, Location location) {
        this.island = island;
        this.location = location;
    }

    public static void create(Optional<Island> island, Location location) {
        StoneGenerator newGenerator = new StoneGenerator(island, location);

        List<StoneGenerator> generators = generatorMap.computeIfAbsent(island, k -> new ArrayList<>());

        generators.removeIf(gen -> gen.getLocation().equals(location));

        generators.add(newGenerator);
    }

    public static void remove(Optional<Island> island, Location location) {
        List<StoneGenerator> list = generatorMap.get(island);

        if (list != null) {
            list.removeIf(gen -> gen.getLocation().equals(location));

            if (list.isEmpty()) {
                generatorMap.remove(island);
            }
        }
    }
    
    public Optional<Island> getIsland() {
        return island;
    }

    public Location getLocation() {
        return location;
    }

    public static HashMap<Optional<Island>, List<StoneGenerator>> getMap() {
        return generatorMap;
    }

    public static ItemStack getItemStack() {
        ItemStack stoniarka = new ItemStack(Material.END_STONE);
        ItemMeta meta = stoniarka.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§l⭐ MAGICZNA STONIARKA ⭐");

            List<String> lore = new ArrayList<>();
            lore.add("§8§m-----------------------");
            lore.add("§7To urządzenie generuje");
            lore.add("§fNieskończone pokłady kamienia.");
            lore.add("");
            lore.add("§e§lINSTRUKCJA:");
            lore.add(" §8» §fPostaw na ziemi, aby zacząć.");
            lore.add(" §8» §fZniszcz kilofem, by odzyskać.");
            lore.add("");
            lore.add("§c§lUWAGA: §7Działa natychmiastowo!");
            lore.add("§8§m-----------------------");
            meta.setLore(lore);

            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);



            stoniarka.setItemMeta(meta);
        }

        NBTUtil.setString(stoniarka, "SkyBlockPlugin", "STONE_GENERATOR");

        return stoniarka;
    }

    public static boolean isStoneGeneratorItem(ItemStack item) {
        if (NBTUtil.hasTag(item, "SkyBlockPlugin") && NBTUtil.getString(item, "SkyBlockPlugin").equals("STONE_GENERATOR")) {
            return true;
        }
        return false;
    }

    public static boolean isStoneGenerator(Location location) {
        Optional<Island> island = BentoBox.getInstance().getIslandsManager().getIslandAt(location);

        List<StoneGenerator> list = generatorMap.get(island);

        if (list == null) return false;

        return list.stream().anyMatch(gen -> gen.getLocation().equals(location));
    }

}
