package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.DropEntry;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.SafeGive;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.BentoBox;

import java.util.concurrent.ThreadLocalRandom;

public class BlockBreak implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location blockLoc = block.getLocation();

        if (StoneGenerator.isStoneGenerator(blockLoc)) {
            if (player.isSneaking()) {
                BentoBox.getInstance().getIslandsManager().getIslandAt(blockLoc).ifPresent(island -> {
                    StoneGenerator.remove(island.getUniqueId(), blockLoc);
                });

                e.setDropItems(false);
                SafeGive.giv(StoneGenerator.getItemStack(), player);
            } else {
                e.setCancelled(true);
                player.sendMessage(Config.MESSAGES_GENERATOR_BREAK_SNEAK_INFO.getString());
                return;
            }
        }

        Location underLoc = blockLoc.clone().subtract(0, 1, 0);
        boolean isGeneratorStone = StoneGenerator.isStoneGenerator(underLoc);

        if (isGeneratorStone) {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (block.getType() == Material.AIR) {
                    block.setType(Material.STONE);
                }
            }, Config.GENERATOR_TIME_TO_REGEN.getInt() * 20L);
        }

        boolean isInMine = block.getWorld().getName().equalsIgnoreCase("kopalnia");

        if (player.hasPermission("group.prekursor") && (isGeneratorStone || isInMine)) {
            Location particleLoc = blockLoc.clone().add(0.5, 0.5, 0.5);

            Particle.DustOptions purpleDust = new Particle.DustOptions(Color.fromRGB(168, 85, 247), 1.5f);
            Particle.DustOptions goldDust = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.0f);

            block.getWorld().spawnParticle(Particle.DUST, particleLoc, 10, 0.4, 0.4, 0.4, purpleDust);
            block.getWorld().spawnParticle(Particle.DUST, particleLoc, 5, 0.3, 0.3, 0.3, goldDust);
            block.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 3, 0.2, 0.2, 0.2, 0.05);
        }

        if (block.getType() == Material.STONE) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (!itemInHand.getType().name().endsWith("_PICKAXE")) return;

            SkyBlockUser user = SkyBlockUser.getSkyBlockUser(player.getUniqueId());
            if (user == null) return;

            e.setDropItems(false);

            int fortuneLevel = itemInHand.getEnchantmentLevel(Enchantment.FORTUNE) + 1;
            boolean isSilkTouch = itemInHand.containsEnchantment(Enchantment.SILK_TOUCH);

            Location dropLoc = blockLoc.clone().add(0.5, 0.5, 0.5);

            for (DropEntry drop : user.getDrops()) {
                if (!drop.enabled()) continue;

                if (ThreadLocalRandom.current().nextDouble(100.0) < (drop.chance() * fortuneLevel)) {
                    ItemStack itemToDrop = drop.dropItem().clone();

                    if (itemToDrop.getType() == Material.STONE && !isSilkTouch) {
                        itemToDrop.setType(Material.COBBLESTONE);
                    }
                    if (user.isMagnetEnabled()) {
                        SafeGive.giv(itemToDrop, e.getPlayer());
                    } else {
                        block.getWorld().dropItemNaturally(dropLoc, itemToDrop);
                    }
                }
            }
        }
    }
}
