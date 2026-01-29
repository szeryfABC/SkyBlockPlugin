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
        if (event.getView().getTitle().isEmpty()) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        Player player = (Player) event.getWhoClicked();

        String mainGuiRaw = ChatColor.stripColor(Config.DROP_MAIN_GUI_NAME.getString());
        String mainGuiPrefix = mainGuiRaw.contains("<drop.level>")
                ? mainGuiRaw.split("<drop.level>")[0]
                : mainGuiRaw;

        String selectionGuiName = ChatColor.stripColor(Config.DROP_SELECTION_GUI_NAME.getString());

        String previewGuiRaw = ChatColor.stripColor(Config.DROP_PREVIEW_GUI_NAME.getString());
        String previewGuiPrefix = previewGuiRaw.contains("<level>")
                ? previewGuiRaw.split("<level>")[0]
                : previewGuiRaw;

        if (title.startsWith(mainGuiPrefix)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            int slot = event.getSlot();

            if (slot == 40) {
                dropMenu.upgradeLevel(player);
            } else if (slot == 38) {
                dropMenu.openPreviewSelection(player);
            }
            else if (slot == 42) {
                dropMenu.toggleMagnet(player);
            }
            else {
                if (slot >= 10 && slot <= 34) {
                    dropMenu.toggleDrop(player, slot);
                }
            }
        }

        else if (title.equals(selectionGuiName)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            int slot = event.getSlot();
            if (slot == Config.DROP_SELECTION_GUI_BACK_SLOT.getInt()) {
                player.playSound(player, Sound.ENTITY_WIND_CHARGE_THROW, 1.0F, 2.0F);
                dropMenu.open(player);
            } else if (slot >= 11 && slot <= 15) {
                int level = slot - 10;
                dropMenu.openLevelPreview(player, level);
            }
        }

        else if (title.startsWith(previewGuiPrefix)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            if (event.getSlot() == Config.DROP_PREVIEW_GUI_BACK_SLOT.getInt()) {
                player.playSound(player, Sound.ENTITY_WIND_CHARGE_THROW, 1.0F, 2.0F);
                dropMenu.openPreviewSelection(player);
            }
        }
    }
}