package me.lemurxd.skyblockplugin.constructors;

import org.bukkit.inventory.ItemStack;

public record DropEntry(ItemStack dropItem, double chance, boolean enabled) {
}
