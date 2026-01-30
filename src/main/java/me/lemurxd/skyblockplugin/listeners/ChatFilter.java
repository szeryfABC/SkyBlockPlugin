package me.lemurxd.skyblockplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatFilter implements Listener {

    private static Pattern forbiddenPattern;

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        String original = e.getMessage();
        String filtered = ChatFilter.filterMessage(original);

        if (!original.equals(filtered)) {
            e.setMessage(filtered);
        }
    }
}
