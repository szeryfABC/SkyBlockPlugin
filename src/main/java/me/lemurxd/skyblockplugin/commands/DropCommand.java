package me.lemurxd.skyblockplugin.commands;

import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.gui.DropMenu;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DropCommand implements CommandExecutor {

    private final DropMenu dropMenu = new DropMenu();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.MESSAGES_ONLY_PLAYERS.getString());
            return true;
        }

        Player player = (Player) sender;

        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);

        dropMenu.open(player);

        return true;
    }
}
