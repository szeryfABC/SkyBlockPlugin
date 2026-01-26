package me.lemurxd.skyblockplugin.gui;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.DropEntry;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.enums.Config;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropMenu {

    public void open(Player player) {
        SkyBlockUser user = SkyBlockUser.getSkyBlockUser(player.getUniqueId());

        if (user == null) {
            player.sendMessage(Config.MESSAGES_DROP_DATABASE_PROBLEM.getString());
            return;
        }

        Inventory inv = Bukkit.createInventory(null, Config.DROP_MAIN_GUI_ROWS.getInt()*9, Config.DROP_MAIN_GUI_NAME.getString().replaceAll("<drop.level>", ""+user.getDropLevel()));

        List<DropEntry> userDrops = user.getDrops();

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        int fortuneLevel = mainHand.getEnchantmentLevel(Enchantment.FORTUNE);
        boolean hasSilkTouch = mainHand.containsEnchantment(Enchantment.SILK_TOUCH);

        for (int i = 0; i < userDrops.size(); i++) {
            int row = i / 7;
            int column = i % 7;

            int slot = 10 + column + (row * 9);

            DropEntry entry = userDrops.get(i);

            ItemStack icon = entry.dropItem().clone();
            if (icon.getType() == Material.STONE && !hasSilkTouch) icon.setType(Material.COBBLESTONE);
            ItemMeta meta = icon.getItemMeta();

            String polishName = getPolishName(icon);

            meta.setDisplayName(polishName);

            double baseChance = entry.chance();
            double boostedChance = baseChance * (fortuneLevel + 1);
            if (boostedChance > 100.0) {
                boostedChance = 100.0;
            }

            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.add(ChatColor.GRAY + "Podstawowa szansa: " + ChatColor.YELLOW + baseChance + "%");

            if (fortuneLevel > 0) {
                lore.add(ChatColor.GRAY + "Bonus z Fortuny " + fortuneLevel + ": " + ChatColor.GOLD + "x" + (fortuneLevel + 1));
                lore.add(ChatColor.GRAY + "Twoja szansa: " + ChatColor.GREEN + ChatColor.BOLD + String.format("%.2f", boostedChance) + "%");
            } else {
                lore.add(ChatColor.GRAY + "Twoja szansa: " + ChatColor.GREEN + ChatColor.BOLD + baseChance + "%");
                lore.add(ChatColor.DARK_GRAY + "(Użyj kilofa z Fortuną aby zwiększyć!)");
            }

            lore.add(" ");

            if (entry.enabled()) {
                lore.add(ChatColor.GREEN + "✔ AKTYWNY");
                lore.add(ChatColor.GRAY + "Kliknij, aby wyłączyć");
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                org.bukkit.inventory.meta.ItemMeta finalMeta = meta;
                finalMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add(ChatColor.RED + "✖ WYŁĄCZONY");
                lore.add(ChatColor.GRAY + "Kliknij, aby włączyć");
            }

            meta.setLore(lore);
            icon.setItemMeta(meta);

            inv.setItem(slot, icon);
        }


        ItemStack previewBtn = new ItemStack(Material.BOOK);
        ItemMeta previewMeta = previewBtn.getItemMeta();
        previewMeta.setDisplayName(ChatColor.AQUA + "Podgląd Dropu");
        previewMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Kliknij, aby zobaczyć jakie przedmioty",
                ChatColor.GRAY + "wypadają na innych poziomach."
        ));
        previewBtn.setItemMeta(previewMeta);
        inv.setItem(38, previewBtn);


        ItemStack upgradeBtn = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta upgradeMeta = upgradeBtn.getItemMeta();
        int currentLevel = user.getDropLevel();
        int nextLevel = currentLevel + 1;
        double cost = getUpgradeCost(currentLevel);

        if (currentLevel >= 5) {
            upgradeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "MAKSYMALNY POZIOM");
            upgradeMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Osiągnięto już maksymalny",
                    ChatColor.GRAY + "poziom dropu: " + ChatColor.AQUA + "5"
            ));
        } else {
            upgradeMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "ULEPSZ POZIOM DROPU");
            List<String> upgradeLore = new ArrayList<>();
            upgradeLore.add(ChatColor.GRAY + "Obecny poziom: " + ChatColor.AQUA + currentLevel);
            upgradeLore.add(ChatColor.GRAY + "Następny poziom: " + ChatColor.AQUA + nextLevel);
            upgradeLore.add(" ");
            upgradeLore.add(ChatColor.GRAY + "Koszt: " + ChatColor.GREEN + cost + "$");

            if (Main.getEconomy() != null) {
                if (Main.getEconomy().has(player, cost)) {
                    upgradeLore.add(ChatColor.YELLOW + "Kliknij, aby kupić ulepszenie!");
                } else {
                    upgradeLore.add(ChatColor.RED + "Nie stać Cię! Brakuje: " + (cost - Main.getEconomy().getBalance(player)) + "$");
                }
            }

            upgradeMeta.setLore(upgradeLore);
        }

        upgradeBtn.setItemMeta(upgradeMeta);
        inv.setItem(40, upgradeBtn);

        fillEmptySlots(inv);
        player.openInventory(inv);
    }

    public void openPreviewSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, Config.DROP_SELECTION_GUI_ROWS.getInt()*9, Config.DROP_SELECTION_GUI_NAME.getString());

        player.playSound(player, Sound.ENTITY_WIND_CHARGE_THROW, 1.0F, 2.0F);

        int[] slots = {11, 12, 13, 14, 15};
        for (int i = 1; i <= 5; i++) {
            ItemStack icon = new ItemStack(Material.PAPER);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Poziom " + i);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Kliknij, aby zobaczyć dropy."));
            icon.setItemMeta(meta);
            inv.setItem(slots[i-1], icon);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(Config.DROP_SELECTION_GUI_BACK.getString());
        back.setItemMeta(backMeta);
        inv.setItem(Config.DROP_SELECTION_GUI_BACK_SLOT.getInt(), back);

        fillEmptySlots(inv);
        player.openInventory(inv);
    }

    public void openLevelPreview(Player player, int level) {
        Inventory inv = Bukkit.createInventory(null, Config.DROP_PREVIEW_GUI_ROWS.getInt()*9, Config.DROP_PREVIEW_GUI_NAME.getString().replaceAll("<level>", ""+level));

        player.playSound(player, Sound.ENTITY_WIND_CHARGE_THROW, 1.0F, 2.0F);

        List<DropEntry> drops = SkyBlockUser.getDropsForLevel(level);
        int startSlot = 9 + (9 - drops.size()) / 2;

        for (int i = 0; i < drops.size(); i++) {
            DropEntry entry = drops.get(i);
            ItemStack icon = entry.dropItem().clone();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(getPolishName(icon));

            List<String> lore = Config.DROP_PREVIEW_GUI_ITEMS_LORE.getStringList().stream().map(s -> s.replaceAll("<chance>", ""+entry.chance())).toList();
            meta.setLore(lore);
            icon.setItemMeta(meta);

            inv.setItem(startSlot + i, icon);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(Config.DROP_PREVIEW_GUI_BACK.getString());
        back.setItemMeta(backMeta);
        inv.setItem(Config.DROP_PREVIEW_GUI_BACK_SLOT.getInt(), back);

        fillEmptySlots(inv);
        player.openInventory(inv);
    }

    public void upgradeLevel(Player player) {
        SkyBlockUser user = SkyBlockUser.getSkyBlockUser(player.getUniqueId());
        if (user == null) return;

        if (user.getDropLevel() >= Config.DROP_LEVELS.getStringList().size()+1) {
            player.sendMessage(Config.MESSAGES_DROP_MAX_LEVEL.getString());
            return;
        }

        double cost = getUpgradeCost(user.getDropLevel());

        if (Main.getEconomy() != null) {
            if (!Main.getEconomy().has(player, cost)) {
                player.sendMessage(Config.MESSAGES_NOT_ENOUGH_MONEY.getString());
                return;
            }
            Main.getEconomy().withdrawPlayer(player, cost);
        }

        int newLevel = user.getDropLevel() + 1;
        user.setDropLevel(newLevel);

        player.sendMessage(Config.MESSAGES_DROP_LEVELUP.getString().replaceAll("<newLevel>", ""+newLevel));

        player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0F, 1.0F);

        open(player);
    }

    public void toggleDrop(Player player, int slot) {
        SkyBlockUser user = SkyBlockUser.getSkyBlockUser(player.getUniqueId());
        if (user == null) return;

        List<DropEntry> drops = user.getDrops();

        int guiColumn = slot % 9;
        if (guiColumn < 1 || guiColumn > 7) {
            return;
        }

        int row = (slot / 9) - 1;
        if (row < 0) {
            return;
        }

        int realIndex = (row * 7) + (guiColumn - 1);

        if (realIndex < 0 || realIndex >= drops.size()) {
            return;
        }

        DropEntry oldEntry = drops.get(realIndex);
        DropEntry newEntry = new DropEntry(oldEntry.dropItem(), oldEntry.chance(), !oldEntry.enabled());
        drops.set(realIndex, newEntry);

        if (newEntry.enabled()) {
            player.playSound(player.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_ON, 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_OFF, 1.0f, 1.0f);
        }

        open(player);
    }

    private void fillEmptySlots(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }

    private double getUpgradeCost(int currentLevel) {
        return Integer.parseInt(Config.DROP_LEVELS.getStringList().get(currentLevel));
    }

    private String getPolishName(ItemStack itemStack) {

        if (MythicBukkit.inst().getItemManager().isMythicItem(itemStack)) {
            return LegacyComponentSerializer.legacySection().serialize(itemStack.getItemMeta().customName());
        }

        Material mat = itemStack.getType();

        for (String s : Config.DROP_POLISH_NAMES.getStringList()) {
            String[] split = s.split(":");

            if (split[0].equalsIgnoreCase(mat.toString())) {
                return split[1];
            }
        }

        return ChatColor.WHITE + mat.name();
    }
}