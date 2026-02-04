package me.lemurxd.skyblockplugin.utils;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;

public class NBTUtil {

    public static ItemStack setString(ItemStack item, String key, String value) {
        if (item == null || item.getItemMeta() == null) return item;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(SkyBlockPlugin.getInstance(), key);

        container.set(namespacedKey, PersistentDataType.STRING, value);

        item.setItemMeta(meta);
        return item;
    }

    @Nullable
    public static String getString(ItemStack item, String key) {
        if (item == null || item.getItemMeta() == null) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(SkyBlockPlugin.getInstance(), key);

        if (container.has(namespacedKey, PersistentDataType.STRING)) {
            return container.get(namespacedKey, PersistentDataType.STRING);
        }
        return null;
    }

    public static ItemStack setInt(ItemStack item, String key, int value) {
        if (item == null || item.getItemMeta() == null) return item;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(SkyBlockPlugin.getInstance(), key);

        container.set(namespacedKey, PersistentDataType.INTEGER, value);

        item.setItemMeta(meta);
        return item;
    }

    public static int getInt(ItemStack item, String key) {
        if (item == null || item.getItemMeta() == null) return 0;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(SkyBlockPlugin.getInstance(), key);

        if (container.has(namespacedKey, PersistentDataType.INTEGER)) {
            //noinspection DataFlowIssue
            return container.get(namespacedKey, PersistentDataType.INTEGER);
        }
        return 0;
    }

    public static boolean hasTag(ItemStack item, String key) {
        if (item == null || item.getItemMeta() == null) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(SkyBlockPlugin.getInstance(), key);

        return container.has(namespacedKey, PersistentDataType.STRING)
                || container.has(namespacedKey, PersistentDataType.INTEGER);
    }
}