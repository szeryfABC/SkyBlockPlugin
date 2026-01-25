package me.lemurxd.skyblockplugin.Listeners;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.DropEntry;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.SafeGive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBreak implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        Location blockLoc = e.getBlock().getLocation();

        if (StoneGenerator.isStoneGenerator(blockLoc)) {
            if (e.getPlayer().isSneaking()) {
                Optional<Island> island = BentoBox.getInstance().getIslandsManager().getIslandAt(blockLoc);
                StoneGenerator.remove(island, blockLoc);

                e.setDropItems(false);
                SafeGive.giv(StoneGenerator.getItemStack(), e.getPlayer());
            } else {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Config.MAIN_PREFIX.getString() + "§cMusisz kucać, aby podnieść stoniarkę!");
                return;
            }
        }

        Location underLoc = blockLoc.clone().subtract(0, 1, 0);

        if (StoneGenerator.isStoneGenerator(underLoc)) {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                blockLoc.getBlock().setType(Material.STONE);
            }, Config.GENERATOR_TIME_TO_REGEN.getInt()*20L);
        }


        if (e.getBlock().getType().equals(Material.STONE) && e.getPlayer().getInventory().getItemInMainHand().getType().name().endsWith("_PICKAXE")) {

            e.setDropItems(false);

            int fortuneLevel = e.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.FORTUNE)+1;
            boolean isSilkTouch = e.getPlayer().getInventory().getItemInMainHand().getEnchantments().keySet().stream().anyMatch(ench -> ench.equals(Enchantment.SILK_TOUCH));

            for (DropEntry drop : SkyBlockUser.getSkyBlockUser(e.getPlayer().getUniqueId()).getDrops()) {
                if (drop.enabled() && ThreadLocalRandom.current().nextDouble(100.0) < drop.chance()*fortuneLevel) {

                    ItemStack itemToDrop = drop.dropItem();

                    if (drop.dropItem().getType().equals(Material.STONE) && !isSilkTouch) {
                        itemToDrop = new ItemStack(Material.COBBLESTONE);
                    }

                    Bukkit.getWorld(e.getBlock().getWorld().getUID()).dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), itemToDrop);
                }
            }
        }

    }

}
