package me.lemurxd.skyblockplugin.gui;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.NBTUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.Optional;

import static me.lemurxd.skyblockplugin.lore.CustomLore.build;

public class HellSmithMenu implements Listener {

    public static void open(Player player) {
        int rows = Config.BLACKSMITH_GUI_ROWS.getInt();
        String title = Config.BLACKSMITH_GUI_TITLE.getString();
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        ItemStack filler = new ItemStack(Material.valueOf(Config.BLACKSMITH_GUI_FILL_ITEM.getString()));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        int rerolls = getRerollCount(hand);

        int moneyMultiplier = rerolls + 1;
        int itemMultiplier = Math.min(rerolls + 1, 20);

        int baseMoney = Config.BLACKSMITH_REROLL_COST_MONEY.getInt();
        int finalMoney = baseMoney * moneyMultiplier;

        List<String> baseItems = Config.BLACKSMITH_REROLL_COST_ITEMS.getStringList();
        List<String> finalItems = new ArrayList<>();

        for (String req : baseItems) {
            String[] split = req.split(":");
            String id = split[0];
            int amount = Integer.parseInt(split[1]);
            finalItems.add(id + ":" + (amount * itemMultiplier));
        }

        ItemStack mendingIcon = createGuiItem(
                Material.valueOf(Config.BLACKSMITH_MENDING_MATERIAL.getString()),
                Config.BLACKSMITH_MENDING_NAME.getString(),
                parseLoreWithCost(Config.BLACKSMITH_MENDING_LORE.getStringList(),
                        Config.BLACKSMITH_MENDING_COST_MONEY.getInt(),
                        Config.BLACKSMITH_MENDING_COST_ITEMS.getStringList())
        );
        inv.setItem(Config.BLACKSMITH_MENDING_SLOT.getInt(), mendingIcon);

        ItemStack rerollIcon = createGuiItem(
                Material.valueOf(Config.BLACKSMITH_REROLL_MATERIAL.getString()),
                Config.BLACKSMITH_REROLL_NAME.getString(),
                parseLoreWithCost(Config.BLACKSMITH_REROLL_LORE.getStringList(),
                        finalMoney,
                        finalItems)
        );
        inv.setItem(Config.BLACKSMITH_REROLL_SLOT.getInt(), rerollIcon);

        ItemStack craftingIcon = createGuiItem(
                Material.valueOf(Config.BLACKSMITH_CRAFTING_MATERIAL.getString()),
                Config.BLACKSMITH_CRAFTING_NAME.getString(),
                Config.BLACKSMITH_CRAFTING_LORE.getStringList()
        );
        inv.setItem(Config.BLACKSMITH_CRAFTING_SLOT.getInt(), craftingIcon);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(Config.BLACKSMITH_GUI_TITLE.getString())) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == Config.BLACKSMITH_MENDING_SLOT.getInt()) {
            handleMending(p);
        }
        else if (slot == Config.BLACKSMITH_REROLL_SLOT.getInt()) {
            handleReroll(p);
        }
        else if (slot == Config.BLACKSMITH_CRAFTING_SLOT.getInt()) {
            CraftingMenu.open(p);
        }
    }

    private void handleMending(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();

        if (hand == null || hand.getType() == Material.AIR) {
            p.sendMessage(Config.MESSAGES_BLACKSMITH_NO_ITEM.getString());
            return;
        }
        if (hand.containsEnchantment(Enchantment.MENDING)) {
            p.sendMessage(Config.MESSAGES_BLACKSMITH_ALREADY_MENDING.getString());
            return;
        }
        if (hand.getType().getMaxDurability() == 0 && hand.getType() != Material.ENCHANTED_BOOK) {
            p.sendMessage(Config.MESSAGES_BLACKSMITH_CANNOT_ENCHANT.getString());
            return;
        }

        int costMoney = Config.BLACKSMITH_MENDING_COST_MONEY.getInt();
        List<String> costItems = Config.BLACKSMITH_MENDING_COST_ITEMS.getStringList();

        if (processPayment(p, costMoney, costItems)) {
            hand.addUnsafeEnchantment(Enchantment.MENDING, 1);
            p.sendMessage(Config.MESSAGES_BLACKSMITH_SUCCESS.getString());
        } else {
            p.sendMessage(Config.MESSAGES_BLACKSMITH_NO_FUNDS.getString());
        }
    }

    private void handleReroll(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();

        if (hand == null || hand.getType() == Material.AIR || !NBTUtil.hasTag(hand, "SkyBlockPlugin")) {
            p.sendMessage(Config.MESSAGES_BLACKSMITH_NO_ITEM.getString());
            return;
        }

        int rerolls = getRerollCount(hand);
        int moneyMultiplier = rerolls + 1;
        int itemMultiplier = Math.min(rerolls + 1, 20);

        double baseMoney = Config.BLACKSMITH_REROLL_COST_MONEY.getInt();
        double finalMoney = baseMoney * moneyMultiplier;

        List<String> baseItems = Config.BLACKSMITH_REROLL_COST_ITEMS.getStringList();
        List<String> finalItems = new ArrayList<>();

        for (String req : baseItems) {
            String[] split = req.split(":");
            String id = split[0];
            int amount = Integer.parseInt(split[1]);
            finalItems.add(id + ":" + (amount * itemMultiplier));
        }

        if (processPayment(p, finalMoney, finalItems)) {

            hand = NBTUtil.remove(hand, "Rarity");

            hand = NBTUtil.setInt(hand, "Rerolls", rerolls + 1);

            p.getInventory().setItemInMainHand(hand);

            build(hand, p, p.getInventory().getHeldItemSlot());

            p.closeInventory();

            p.sendMessage(Config.MESSAGES_BLACKSMITH_SUCCESS.getString());
        } else {
            p.sendMessage(Config.MESSAGES_BLACKSMITH_NO_FUNDS.getString());
        }
    }

    private static int getRerollCount(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        try {
            Integer val = NBTUtil.getInt(item, "Rerolls");
            if (val != null) {
                return val;
            }
        } catch (Exception ignored) {}
        return 0;
    }


    private boolean processPayment(Player p, double money, List<String> items) {
        if (SkyBlockPlugin.getEconomy() != null && SkyBlockPlugin.getEconomy().getBalance(p) < money) {
            return false;
        }

        for (String req : items) {
            String[] split = req.split(":");
            String mythicId = split[0];
            int amount = Integer.parseInt(split[1]);

            if (!hasMythicItem(p, mythicId, amount)) {
                return false;
            }
        }

        if (SkyBlockPlugin.getEconomy() != null && money > 0) {
            SkyBlockPlugin.getEconomy().withdrawPlayer(p, money);
        }

        for (String req : items) {
            String[] split = req.split(":");
            removeMythicItem(p, split[0], Integer.parseInt(split[1]));
        }

        return true;
    }

    private static List<String> parseLoreWithCost(List<String> originalLore, int money, List<String> items) {
        List<String> newLore = new ArrayList<>();
        for (String line : originalLore) {
            if (line.contains("<cost>")) {
                if (money > 0) {
                    newLore.add("&7- &6$" + money);
                }
                for (String itemStr : items) {
                    String[] split = itemStr.split(":");
                    String id = split[0];
                    String amount = split[1];
                    String displayName = getMythicName(id);
                    newLore.add("&7- &f" + amount + "x " + displayName);
                }
            } else {
                newLore.add(line);
            }
        }
        return newLore;
    }

    private static String getMythicName(String internalName) {
        try {
            Optional<MythicItem> item = MythicBukkit.inst().getItemManager().getItem(internalName);
            if (item.isPresent()) {
                String rawName = item.get().getDisplayName();

                Component component = MiniMessage.miniMessage().deserialize(rawName);
                return LegacyComponentSerializer.builder()
                        .hexColors()
                        .useUnusualXRepeatedCharacterHexFormat()
                        .build()
                        .serialize(component);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return internalName;
    }

    private boolean hasMythicItem(Player p, String internalName, int amountNeeded) {
        int found = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null) continue;
            String type = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(is);
            if (type != null && type.equals(internalName)) {
                found += is.getAmount();
            }
        }
        return found >= amountNeeded;
    }

    private void removeMythicItem(Player p, String internalName, int amountToRemove) {
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null) continue;
            String type = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(is);

            if (type != null && type.equals(internalName)) {
                int amount = is.getAmount();
                if (amount <= amountToRemove) {
                    amountToRemove -= amount;
                    p.getInventory().remove(is);
                } else {
                    is.setAmount(amount - amountToRemove);
                    amountToRemove = 0;
                }
                if (amountToRemove <= 0) break;
            }
        }
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
