package me.lemurxd.skyblockplugin.commands;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.BasicUtils;
import me.lemurxd.skyblockplugin.utils.SafeGive;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OrbCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.MESSAGES_ONLY_PLAYERS.getString());
            return true;
        }

        Player player = (Player) sender;

        SkyBlockUser user = SkyBlockUser.getSkyBlockUser(player.getUniqueId());

        long currentTime = System.currentTimeMillis();
        long lastUsed = user.getLastOrbUsage();

        long cooldownTime = Config.ORB_COOLDOWN.getInt() * 60 * 60 * 1000L;

        if (currentTime - lastUsed < cooldownTime) {
            long timeLeftMillis = (lastUsed + cooldownTime) - currentTime;

            String formattedTime = BasicUtils.formatTime(timeLeftMillis);

            player.sendMessage(Config.MESSAGES_ORB_COOLDOWN.getString().replaceAll("<formattedTime>", formattedTime));
            return true;
        }

        SafeGive.giv(MythicBukkit.inst().getItemManager().getItemStack(Config.ORB_ITEM_NAME.getString()), player);

        user.setLastOrbUsage(currentTime);

        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        player.sendMessage(Config.MESSAGES_ORB_GIVE.getString());

        return true;
    }

}
