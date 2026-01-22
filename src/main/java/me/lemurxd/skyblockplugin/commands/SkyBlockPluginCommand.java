package me.lemurxd.skyblockplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkyBlockPluginCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("sbp.admin")) {
            sender.sendMessage("§cBrak uprawnień!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cPoprawne użycie: /sbp give <przedmiot> [gracz]");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {

            if (args[1].equalsIgnoreCase("stoniarka")) {

                Player target;

                if (args.length == 3) {
                    target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage("§cGracz " + args[2] + " jest offline!");
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cKonsola musi podać nick gracza! /sbp give stoniarka <nick>");
                        return true;
                    }
                    target = (Player) sender;
                }


                sender.sendMessage("§aPomyślnie dano stoniarkę graczowi " + target.getName());
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("give");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.add("stoniarka");
        }

        return completions;
    }
}
