package me.lemurxd.skyblockplugin.Listeners;

import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.gui.DropMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class Inventory implements Listener {

    private final DropMenu dropMenu = new DropMenu();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());

        if (title.startsWith("Zarządzanie Dropem")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            if (slot == 31) {
                dropMenu.upgradeLevel(player);
            }
            else if (slot == 29) {
                dropMenu.openPreviewSelection(player);
            }
            else {
                if (slot >= 9 && slot <= 17) {
                    dropMenu.toggleDrop(player, slot);
                }
            }
        }
        else if (title.equals("Wybierz poziom do podglądu")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();

            int slot = event.getSlot();
            if (slot == 22) {
                dropMenu.open(player);
            }
            else if (slot >= 11 && slot <= 15) {
                int level = slot - 10;
                dropMenu.openLevelPreview(player, level);
            }
        }

        else if (title.startsWith("Podgląd: Poziom")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == 31) {
                dropMenu.open(player);
            }
        }
    }
}