package me.lemurxd.skyblockplugin.constructors;

import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.NBTUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;

public class StoneGenerator {

    private static HashMap<String, List<StoneGenerator>> generatorMap = new HashMap<>();

    private Location location;
    private String islandUuid;
    
    public StoneGenerator(String islandUuid, Location location) {
        this.islandUuid = islandUuid;
        this.location = location;
    }

    public static void create(String islandUuid, Location location) {
        StoneGenerator newGenerator = new StoneGenerator(islandUuid, location);

        List<StoneGenerator> generators = generatorMap.computeIfAbsent(islandUuid, k -> new ArrayList<>());

        generators.removeIf(gen -> gen.getLocation().equals(location));

        generators.add(newGenerator);
    }

    public static void remove(String island, Location location) {
        List<StoneGenerator> list = generatorMap.get(island);

        if (list != null) {
            list.removeIf(gen -> gen.getLocation().equals(location));

            if (list.isEmpty()) {
                generatorMap.remove(island);
            }
        }
    }
    
    public String getIsland() {
        return islandUuid;
    }

    public Location getLocation() {
        return location;
    }

    public static HashMap<String, List<StoneGenerator>> getMap() {
        return generatorMap;
    }

    public static ItemStack getItemStack() {
        ItemStack stoniarka = new ItemStack(Material.getMaterial(Config.GENERATOR_ITEM_MATERIAL.getString()));
        ItemMeta meta = stoniarka.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(Config.GENERATOR_ITEM_NAME.getString());

            List<String> lore = Config.GENERATOR_ITEM_LORE.getStringList();
            meta.setLore(lore);

            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);



            stoniarka.setItemMeta(meta);
        }

        NBTUtil.setString(stoniarka, Config.GENERAOTOR_ITEM_NBT_KEY.getString(), Config.GENERAOTOR_ITEM_NBT_KEY.getString());

        return stoniarka;
    }

    public static boolean isStoneGeneratorItem(ItemStack item) {
        if (NBTUtil.hasTag(item, Config.GENERAOTOR_ITEM_NBT_KEY.getString()) && NBTUtil.getString(item, Config.GENERAOTOR_ITEM_NBT_KEY.getString()).equals(Config.GENERAOTOR_ITEM_NBT_KEY.getString())) {
            return true;
        }
        return false;
    }

    public static boolean isStoneGenerator(Location location) {
        String island = BentoBox.getInstance().getIslandsManager().getIslandAt(location).get().getUniqueId();

        List<StoneGenerator> list = generatorMap.get(island);

        if (list == null) return false;

        return list.stream().anyMatch(gen -> gen.getLocation().equals(location));
    }

}
