package me.lemurxd.skyblockplugin.gui;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HellHordesMenu implements Listener {

    public static void open(Player player) {
        int rows = Config.HORDES_GUI_ROWS.getInt();
        String title = Config.HORDES_GUI_TITLE.getString().replace("&", "§");
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        ItemStack filler = new ItemStack(Material.valueOf(Config.HORDES_GUI_FILL_ITEM.getString()));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ItemStack connectIcon = createGuiItem(
                Material.valueOf(Config.HORDES_CONNECT_MATERIAL.getString()),
                Config.HORDES_CONNECT_NAME.getString(),
                Config.HORDES_CONNECT_LORE.getStringList()
        );
        inv.setItem(Config.HORDES_CONNECT_SLOT.getInt(), connectIcon);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = Config.HORDES_GUI_TITLE.getString().replace("&", "§");
        if (!e.getView().getTitle().equals(title)) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == Config.HORDES_CONNECT_SLOT.getInt()) {
            p.closeInventory();
            p.sendMessage("§aŁączenie z Piekielnymi Hordami...");
            connectToServer(p, "piekielnehordy");
        }
    }

    private void connectToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        player.sendPluginMessage(SkyBlockPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }

    private static ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        List<String> coloredLore = new ArrayList<>();
        if (lore != null) {
            for (String s : lore) {
                coloredLore.add(s.replace("&", "§"));
            }
        }
        meta.setLore(coloredLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
