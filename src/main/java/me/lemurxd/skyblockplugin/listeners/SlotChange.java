package me.lemurxd.skyblockplugin.listeners;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import me.lemurxd.skyblockplugin.lore.CustomLore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SlotChange implements Listener {

    @EventHandler
    public void PlayerSlotChange(PlayerInventorySlotChangeEvent e) {
        CustomLore.build(e.getNewItemStack(), e.getPlayer(), e.getSlot());
    }

}
