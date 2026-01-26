package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.gui.DropMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class Inventory implements Listener {

    private final DropMenu dropMenu = new DropMenu();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());

        if (title.startsWith(ChatColor.stripColor(Config.DROP_MAIN_GUI_NAME.getString()).replaceAll("<drop.level>", ""))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            if (slot == 40) {
                dropMenu.upgradeLevel(player);
            }
            else if (slot == 38) {
                dropMenu.openPreviewSelection(player);
            }
            else {
                if (slot >= 9 && slot <= 25) {
                    dropMenu.toggleDrop(player, slot);
                }
            }
        }
        else if (title.equals(ChatColor.stripColor(Config.DROP_SELECTION_GUI_NAME.getString()))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();

            int slot = event.getSlot();
            if (slot == Config.DROP_SELECTION_GUI_BACK_SLOT.getInt()) {
                player.playSound(player, Sound.ENTITY_WIND_CHARGE_THROW, 1.0F, 2.0F);
                dropMenu.open(player);
            }
            else if (slot >= 11 && slot <= 15) {
                int level = slot - 10;
                dropMenu.openLevelPreview(player, level);
            }
        }

        else if (title.startsWith(ChatColor.stripColor(Config.DROP_SELECTION_GUI_NAME.getString()).replaceAll("<level>", ""))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == Config.DROP_SELECTION_GUI_BACK_SLOT.getInt()) {
                player.playSound(player, Sound.ENTITY_WIND_CHARGE_THROW, 1.0F, 2.0F);
                dropMenu.open(player);
            }
        }
    }
}