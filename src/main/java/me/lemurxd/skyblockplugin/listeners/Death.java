package me.lemurxd.skyblockplugin.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.lemurxd.skyblockplugin.utils.NBTUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Death implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            drops.set(i, breakMending(drop));
        }

        if (event.getKeepInventory()) {
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                inv.setItem(i, breakMending(item));
            }
        }
    }

    private ItemStack breakMending(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;

        if (!item.containsEnchantment(Enchantment.MENDING)) return item;

        String mythicType = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
        if (mythicType == null) return item;

        item = NBTUtil.setString(item, "deadMending", "true");

        return item;
    }

}
