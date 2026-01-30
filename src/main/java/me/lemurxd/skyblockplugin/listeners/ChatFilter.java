package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatFilter implements Listener {

    private static Pattern forbiddenPattern;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public static void loadFilter(List<String> badWords) {
        if (badWords == null || badWords.isEmpty()) {
            forbiddenPattern = null;
            return;
        }

        String patternString = badWords.stream()
                .map(Pattern::quote)
                .map(w -> "\\b" + w + "\\b")
                .collect(Collectors.joining("|"));

        forbiddenPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
    }


    public static String filterMessage(String message) {
        if (forbiddenPattern == null) return message;

        Matcher matcher = forbiddenPattern.matcher(message);

        return matcher.replaceAll("*uwu*");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (!player.hasPermission("chat.bypass")) {
            long now = System.currentTimeMillis();
            long lastMessageTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);

            if (now - lastMessageTime < Config.CHAT_COOLDOWN.getInt()) {
                double left = (Config.CHAT_COOLDOWN.getInt() - (now - lastMessageTime)) / 1000.0;
                player.sendMessage(Config.MAIN_PREFIX.getString() + String.format("§cWolniej! Możesz napisać za %.1fs.", left));
                e.setCancelled(true);
                return;
            }

            cooldowns.put(player.getUniqueId(), now);
        }


        String original = e.getMessage();
        String filtered = ChatFilter.filterMessage(original);

        if (!original.equals(filtered)) {
            e.setMessage(filtered);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldowns.remove(e.getPlayer().getUniqueId());
    }
}
