package me.lemurxd.skyblockplugin.gui;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.BaseFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
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

public class NetherMenu implements Listener {

    private Economy economy = SkyBlockPlugin.getEconomy();

    public void open(Player player) {
        int rows = Config.NETHER_MENU_ROWS.getInt();
        String title = Config.NETHER_MENU_TITLE.getString();
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        ItemStack filler = new ItemStack(Material.valueOf(Config.NETHER_MENU_FILL_ITEM.getString()));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        String uuid = player.getUniqueId().toString();

        boolean hasKowal = SkyBlockPlugin.getData().isKowalUnlocked(uuid);
        ItemStack kowalItem = createItem(
                Material.valueOf(Config.NETHER_KOWAL_MATERIAL.getString()),
                Config.NETHER_KOWAL_NAME.getString(),
                hasKowal ? Config.NETHER_KOWAL_LORE_BOUGHT.getStringList() : Config.NETHER_KOWAL_LORE.getStringList(),
                Config.NETHER_KOWAL_COST.getInt(),
                hasKowal
        );
        inv.setItem(Config.NETHER_KOWAL_SLOT.getInt(), kowalItem);

        boolean hasHordy = SkyBlockPlugin.getData().isHordyUnlocked(uuid);
        ItemStack hordyItem = createItem(
                Material.valueOf(Config.NETHER_HORDY_MATERIAL.getString()),
                Config.NETHER_HORDY_NAME.getString(),
                hasHordy ? Config.NETHER_HORDY_LORE_BOUGHT.getStringList() : Config.NETHER_HORDY_LORE.getStringList(),
                Config.NETHER_HORDY_COST.getInt(),
                hasHordy
        );
        inv.setItem(Config.NETHER_HORDY_SLOT.getInt(), hordyItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(Config.NETHER_MENU_TITLE.getString())) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        Player p = (Player) e.getWhoClicked();
        String uuid = p.getUniqueId().toString();
        int slot = e.getSlot();

        if (slot == Config.NETHER_KOWAL_SLOT.getInt()) {
            if (SkyBlockPlugin.getData().isKowalUnlocked(uuid)) {
                p.sendMessage(Config.NETHER_MSG_ALREADY_OWNED.getString());
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            int cost = Config.NETHER_KOWAL_COST.getInt();
            if (buyFeature(p, cost)) {
                SkyBlockPlugin.getData().setKowal(uuid, true);
                p.sendMessage(Config.NETHER_MSG_BOUGHT.getString().replace("<feature>", "Siedziba Kowala"));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                //TODO: Tutaj żeby działało

                p.closeInventory();
            }
        }

        else if (slot == Config.NETHER_HORDY_SLOT.getInt()) {
            if (SkyBlockPlugin.getData().isHordyUnlocked(uuid)) {
                p.sendMessage(Config.NETHER_MSG_ALREADY_OWNED.getString());
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            int cost = Config.NETHER_HORDY_COST.getInt();
            if (buyFeature(p, cost)) {
                SkyBlockPlugin.getData().setHordy(uuid, true);
                p.sendMessage(Config.NETHER_MSG_BOUGHT.getString().replace("<feature>", "Piekielne Hordy"));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                //TODO: Tutaj żeby działało

                p.closeInventory();
            }
        }
    }

    private boolean buyFeature(Player p, int cost) {
        if (economy == null) {
            p.sendMessage("§cBłąd: Brak wtyczki Vault/Economy!");
            return false;
        }

        if (economy.getBalance(p) >= cost) {
            economy.withdrawPlayer(p, cost);
            return true;
        } else {
            p.sendMessage(Config.MESSAGES_NOT_ENOUGH_MONEY.getString());
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> lore, int cost, boolean bought) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> formattedLore = new ArrayList<>();
        for (String line : lore) {
            formattedLore.add(line.replace("<cost>", String.valueOf(cost)).replace("&", "§"));
        }

        if (bought) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setLore(formattedLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}
