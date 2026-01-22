package me.lemurxd.skyblockplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class SafeGive {

    public SafeGive(ItemStack item, Player p) {
        Iterator<ItemStack> iter = p.getInventory().addItem(item).values().iterator();
        while (iter.hasNext()) {
            p.getWorld().dropItemNaturally(p.getLocation(), iter.next());
        }
    }
}
