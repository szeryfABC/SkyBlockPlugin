package me.lemurxd.skyblockplugin.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

public class MythicItemProtection implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        org.bukkit.inventory.Inventory topInv = event.getView().getTopInventory();
        InventoryType topType = topInv.getType();

        // Zezwalamy na bezpieczne inwentarze (w tym MERCHANT - Villager)
        if (topType == InventoryType.CHEST ||
                topType == InventoryType.ENDER_CHEST ||
                topType == InventoryType.BARREL ||
                topType == InventoryType.SHULKER_BOX ||
                topType == InventoryType.PLAYER ||
                topType == InventoryType.MERCHANT) {
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInv)) {

                int hotbarSlot = event.getHotbarButton();
                ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarSlot);

                if (MythicBukkit.inst().getItemManager().isMythicItem(hotbarItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInv)) {

                Player player = (Player) event.getWhoClicked();
                ItemStack offhandItem = player.getInventory().getItemInOffHand();

                if (MythicBukkit.inst().getItemManager().isMythicItem(offhandItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (MythicBukkit.inst().getItemManager().isMythicItem(clickedItem)) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getClickedInventory().equals(topInv)) {
            if (MythicBukkit.inst().getItemManager().isMythicItem(cursorItem) || MythicBukkit.inst().getItemManager().isMythicItem(clickedItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        org.bukkit.inventory.Inventory topInv = event.getView().getTopInventory();
        InventoryType topType = topInv.getType();

        // 1. Zezwalamy na bezpieczne inwentarze (w tym MERCHANT - Villager)
        if (topType == InventoryType.CHEST ||
                topType == InventoryType.ENDER_CHEST ||
                topType == InventoryType.BARREL ||
                topType == InventoryType.SHULKER_BOX ||
                topType == InventoryType.PLAYER ||
                topType == InventoryType.MERCHANT) {
            return;
        }

        if (MythicBukkit.inst().getItemManager().isMythicItem(event.getOldCursor())) {
            for (int slot : event.getRawSlots()) {
                if (slot < topInv.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}