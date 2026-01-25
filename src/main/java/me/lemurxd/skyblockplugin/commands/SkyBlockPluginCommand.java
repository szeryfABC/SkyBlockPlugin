package me.lemurxd.skyblockplugin.commands;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.SafeGive;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SkyBlockPluginCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("sbp.admin")) {
            sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cBrak uprawnień!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cPoprawne użycie:");
            sender.sendMessage(Config.MAIN_PREFIX.getString() + " §7/sbp give stoniarka [gracz]");
            sender.sendMessage(Config.MAIN_PREFIX.getString() + " §7/sbp drop set <gracz> <poziom>");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("give")) {

            if (args[1].equalsIgnoreCase("stoniarka")) {
                Player target;

                if (args.length == 3) {
                    target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cGracz " + args[2] + " jest offline!");
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cKonsola musi podać nick gracza! /sbp give stoniarka <nick>");
                        return true;
                    }
                    target = (Player) sender;
                }

                SafeGive.giv(StoneGenerator.getItemStack(), target);
                sender.sendMessage(Config.MAIN_PREFIX.getString() + " §aPomyślnie dano stoniarkę graczowi " + target.getName());
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("drop")) {
            if (args.length >= 4 && args[1].equalsIgnoreCase("set")) {
                String targetName = args[2];
                int level;

                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cPodana wartość '" + args[3] + "' nie jest liczbą!");
                    return true;
                }

                Player target = Bukkit.getPlayer(targetName);

                if (target != null) {
                    SkyBlockUser user = SkyBlockUser.getSkyBlockUser(target.getUniqueId());
                    if (user != null) {
                        user.setDropLevel(level);
                        sender.sendMessage(Config.MAIN_PREFIX.getString() + " §aUstawiono poziom dropu gracza " + target.getName() + " na " + level + " (Online).");

                    } else {
                        sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cBłąd: Nie znaleziono danych użytkownika w pamięci (spróbuj relog)!");
                    }
                } else {
                    sender.sendMessage(Config.MAIN_PREFIX.getString() + " §7Gracz offline. Aktualizuję dane w bazie...");

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
                    UUID targetUUID = offlinePlayer.getUniqueId();

                    Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                        SkyBlockUser user = Main.getUserDatabase().loadUser(targetUUID);

                        if (user != null) {
                            user.setDropLevel(level);

                            Main.getUserDatabase().saveUser(user);

                            sender.sendMessage(Config.MAIN_PREFIX.getString() + " §aPomyślnie zaktualizowano poziom gracza " + targetName + " na " + level + " w bazie danych.");
                        } else {
                            sender.sendMessage(Config.MAIN_PREFIX.getString() + " §cGracz " + targetName + " nie istnieje w bazie danych pluginu.");
                        }
                    });
                }
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
            completions.add("drop");
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                completions.add("stoniarka");
            } else if (args[0].equalsIgnoreCase("drop")) {
                completions.add("set");
            }
        }
        else if (args.length == 3) {
            return null;
        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("drop") && args[1].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
            }
        }

        return completions;
    }
}
