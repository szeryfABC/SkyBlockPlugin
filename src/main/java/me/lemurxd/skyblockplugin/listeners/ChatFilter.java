package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatFilter implements Listener {

    private static AhoCorasick acAlgorithm;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    private static final double PROFANITY_THRESHOLD = 0.60;
    private static final int MAX_SAFE_WORD_LENGTH = 13;

    private static final Map<Character, Character> LEET_MAP = new HashMap<>();
    static {
        LEET_MAP.put('1', 'i');
        LEET_MAP.put('!', 'i');
        LEET_MAP.put('3', 'e');
        LEET_MAP.put('4', 'a');
        LEET_MAP.put('@', 'a');
        LEET_MAP.put('5', 's');
        LEET_MAP.put('$', 's');
        LEET_MAP.put('0', 'o');
        LEET_MAP.put('7', 't');
        LEET_MAP.put('+', 't');
        LEET_MAP.put('(', 'c');
        LEET_MAP.put('_', ' ');
    }

    public static void loadFilter(List<String> badWords) {
        acAlgorithm = new AhoCorasick();
        if (badWords != null) {
            for (String word : badWords) {
                acAlgorithm.addWord(word.toLowerCase());
            }
        }
        acAlgorithm.buildFailureLinks();
    }

    public static String filterMessage(String message) {
        if (acAlgorithm == null || message == null || message.isEmpty()) return message;

        StringBuilder cleanTextBuilder = new StringBuilder();
        int[] indexMap = new int[message.length()];
        int cleanIndex = 0;
        char lastChar = 0;

        for (int i = 0; i < message.length(); i++) {
            char originalChar = message.charAt(i);
            char c = Character.toLowerCase(originalChar);
            c = LEET_MAP.getOrDefault(c, c);

            if (!Character.isLetterOrDigit(c)) continue;
            if (c == lastChar) continue;

            cleanTextBuilder.append(c);
            indexMap[cleanIndex] = i;
            cleanIndex++;
            lastChar = c;
        }

        String cleanText = cleanTextBuilder.toString();
        List<IntRange> matches = acAlgorithm.search(cleanText);

        if (matches.isEmpty()) return message;

        matches = mergeIntervals(matches);
        List<IntRange> finalReplacementRanges = new ArrayList<>();

        for (int i = matches.size() - 1; i >= 0; i--) {
            IntRange match = matches.get(i);
            if (match.start >= cleanIndex || match.end > cleanIndex) continue;

            int detectedStart = indexMap[match.start];
            int detectedEnd = indexMap[match.end - 1] + 1;

            int wordStart = findWordStart(message, detectedStart);
            int wordEnd = findWordEnd(message, detectedEnd);

            int cleanBadLength = match.end - match.start;

            String fullOriginalWord = message.substring(wordStart, wordEnd);

            int cleanFullWordLength = getCollapsedLength(fullOriginalWord);

            if (cleanFullWordLength == 0) cleanFullWordLength = 1;

            double ratio = (double) cleanBadLength / cleanFullWordLength;

            if (cleanFullWordLength <= MAX_SAFE_WORD_LENGTH && ratio < PROFANITY_THRESHOLD) {
                continue;
            }

            finalReplacementRanges.add(new IntRange(wordStart, wordEnd));
        }

        finalReplacementRanges = mergeIntervals(finalReplacementRanges);

        StringBuilder result = new StringBuilder(message);
        List<String> replacements = Config.CENZURA_REPLACE.getStringList();

        for (int i = finalReplacementRanges.size() - 1; i >= 0; i--) {
            IntRange range = finalReplacementRanges.get(i);
            String replacement = replacements.isEmpty() ? "*uwu*" : replacements.get(random.nextInt(replacements.size()));
            result.replace(range.start, range.end, replacement);
        }

        return result.toString();
    }

    private static int getCollapsedLength(String word) {
        if (word == null || word.isEmpty()) return 0;
        int length = 0;
        char last = 0;

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!Character.isLetterOrDigit(c)) continue;

            c = Character.toLowerCase(c);
            c = LEET_MAP.getOrDefault(c, c);

            if (c == last) continue;

            length++;
            last = c;
        }
        return length;
    }

    private static int findWordStart(String text, int index) {
        while (index > 0 && !Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private static int findWordEnd(String text, int index) {
        while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static List<IntRange> mergeIntervals(List<IntRange> ranges) {
        if (ranges.size() <= 1) return ranges;
        ranges.sort(Comparator.comparingInt(r -> r.start));

        List<IntRange> merged = new ArrayList<>();
        IntRange current = ranges.get(0);

        for (int i = 1; i < ranges.size(); i++) {
            IntRange next = ranges.get(i);
            if (current.end >= next.start) {
                current = new IntRange(current.start, Math.max(current.end, next.end));
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }


    private static class IntRange {
        int start, end;
        IntRange(int start, int end) { this.start = start; this.end = end; }
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode failure;
        boolean isEndOfWord = false;
        int depth = 0;
    }

    private static class AhoCorasick {
        private final TrieNode root = new TrieNode();

        public void addWord(String word) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node.children.computeIfAbsent(c, k -> new TrieNode());
                node = node.children.get(c);
            }
            node.isEndOfWord = true;
            node.depth = word.length();
        }

        public void buildFailureLinks() {
            Queue<TrieNode> queue = new LinkedList<>();
            for (TrieNode child : root.children.values()) {
                child.failure = root;
                queue.add(child);
            }
            while (!queue.isEmpty()) {
                TrieNode current = queue.poll();
                for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                    char c = entry.getKey();
                    TrieNode child = entry.getValue();
                    TrieNode temp = current.failure;
                    while (temp != null && !temp.children.containsKey(c)) {
                        temp = temp.failure;
                    }
                    child.failure = (temp == null) ? root : temp.children.get(c);
                    queue.add(child);
                }
            }
        }

        public List<IntRange> search(String text) {
            List<IntRange> found = new ArrayList<>();
            TrieNode current = root;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                while (current != null && !current.children.containsKey(c)) {
                    current = current.failure;
                }
                if (current == null) {
                    current = root;
                    continue;
                }
                current = current.children.get(c);
                TrieNode temp = current;
                while (temp != root) {
                    if (temp.isEndOfWord) {
                        found.add(new IntRange(i - temp.depth + 1, i + 1));
                    }
                    temp = temp.failure;
                }
            }
            return found;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (!player.hasPermission("chat.bypass")) {
            long now = System.currentTimeMillis();
            long lastMessageTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);
            int cooldownTime = Config.CHAT_COOLDOWN.getInt();

            if (now - lastMessageTime < cooldownTime) {
                double left = (cooldownTime - (now - lastMessageTime)) / 1000.0;
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
