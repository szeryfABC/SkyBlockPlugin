package me.lemurxd.skyblockplugin.enums;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A robust, centralized configuration management system based on a monolithic Enum pattern.
 * Handles data serialization, type-safe retrieval, and dynamic reloading.
 *
 * @author lemurxd - Refined implementation and project-specific adaptations.
 * @version 1.0
 *
 * @credits Core architecture originally designed by szumielxd.
 * @note Integrated and modified with explicit permission from the original author.
 */
public enum Config {
    //Main
    MAIN_PREFIX("main.variables_prefix", "&a&lOF &f| ", true),

    //Messages
    MESSAGES_ONLY_PLAYERS("messages.only_for_players", "<prefix>&cTa komenda jest dostępna tylko dla graczy!", true),
    MESSAGES_NOT_ENOUGH_MONEY("messages.not_enough_money", "<prefix>&cNie masz wystarczająco pieniędzy!"),
    MESSAGES_ORB_COOLDOWN("messages.orb.coldown_message", "<prefix>&cMusisz odczekać jeszcze: <formattedTime>", true),
    MESSAGES_ORB_GIVE("messages.org.given_message", "<prefix>&aOdebrałeś Magicznego Orba!", true),
    MESSAGES_DROP_DATABASE_PROBLEM("messages.drop.database_problem", "<prefix>&cBłąd: Nie załadowano Twoich danych!", true),
    MESSAGES_DROP_MAX_LEVEL("messages.drop.max_level", "<prefix>&cOsiągnąłeś już maksymalny poziom!", true),
    MESSAGES_DROP_LEVELUP("messages.drop.levelup", "<prefix>Ulepszyłeś drop na <level> poziom!", true),
    MESSAGES_GENERATOR_BREAK_SNEAK_INFO("messages.generator.sneak_when_break", "<prefix>Musisz kucać, aby podnieść stoniarkę!", true),

    //DROP
    DROP_LEVELS("drop.levels_cost", Arrays.asList("5000", "20000", "50000", "100000")),
    DROP_DROPS("drop.drop_config", Arrays.asList("STONE:100.0<n>COAL:1.0<n>COPPER_INGOT:0.8", "STONE:1000<n>COAL:2<n>IRON_INGOT:0.5<n>COPPER_INGOT:1.5<n>GOLD_INGOT:0.3<n>mitycznyDiament:0.001", "STONE:100.0<n>COAL:6.0<n>IRON_INGOT:2.5<n>COPPER_INGOT:5.0<n>GOLD_INGOT:1.0<n>DIAMOND:0.3<n>EMERALD:0.1<n>mitycznyDiament:0.01", "STONE:100.0<n>COAL:10.0<n>IRON_INGOT:5.0<n>COPPER_INGOT:8.5<n>GOLD_INGOT:3.0<n>DIAMOND:0.8<n>EMERALD:0.5<n>mitycznyDiament:0.05")),


    DROP_MAIN_GUI_ROWS("drop.gui.main.rows", 5),
    DROP_MAIN_GUI_NAME("drop.gui.main.name", "&7Zarządzanie Dropem (Lvl: <drop.level>)", true),
    DROP_MAIN_GUI_ITEMS_BASIC_LORE("drop.gui.main.item.basic_lore", "&7Podstawowa szansa: &e<baseChance>%", true),
    DROP_MAIN_GUI_ITEMS_LORE_FORTUNE("drop.gui.main.item.lore_with_fortune", Arrays.asList("&7Bonus z Fortuny <fortuneLevel>: &ex<bonusLevel>", "&7Twoja szansa: &a&l<boostChance>%"), true),
    DROP_MAIN_GUI_ITEMS_LORE_NO_FORTUNE("drop.gui.main.item.lore_without_fortune", Arrays.asList("&7Twoja szansa: &a&l<baseChance>%", "&8(Użyj kilofa z Fortuną aby zwiększyć!)"), true),
    DROP_MAIN_GUI_ITEMS_LORE_ACTIVE("drop.gui.main.item.lore_active", Arrays.asList("&a✔ AKTYWNY", "&7Kliknij, aby wyłączyć"), true),
    DROP_MAIN_GUI_ITEMS_LORE_NOT_ACTIVE("drop.gui.main.item.lore_not_active", Arrays.asList("&c✖ WYŁĄCZONY", "&7Kliknij, aby włączyć"), true),

    DROP_PREVIEW_GUI_ROWS("drop.gui.preview.rows", 3),
    DROP_PREVIEW_GUI_NAME("drop.gui.preview.name", "&8Podgląd: Poziom <level>", true),
    DROP_PREVIEW_GUI_ITEMS_LORE("drop.gui.preview.items_lore", Arrays.asList("&7Szansa bazowa: <chance>%"), true),
    DROP_PREVIEW_GUI_BACK("drop.gui.preview.back.name", "&cPowrót do menu", true),
    DROP_PREVIEW_GUI_BACK_SLOT("drop.gui.preview.back.slot", 31),

    DROP_SELECTION_GUI_ROWS("drop.gui.selection.rows", 1),
    DROP_SELECTION_GUI_NAME("drop.gui.selection.name", "&8Wybierz poziom do podglądu", true),
    DROP_SELECTION_GUI_BACK("drop.gui.selection.back.name", "&cPowrót do menu", true),
    DROP_SELECTION_GUI_BACK_SLOT("drop.gui.selection.back.slot", 22),

    DROP_MAGNET_GUI_NAME("drop.gui.magnet.name", "&d&lAUTOMATYCZNY DROP", true),
    DROP_MAGNET_GUI_LORE("drop.gui.magnet.lore", Arrays.asList(
            "&7Status: <status>",
            " ",
            "&eKliknij, aby przełączyć!"
    ), true),
    DROP_MAGNET_STATUS_ON("drop.gui.magnet.status.on", "&aWŁĄCZONY", true),
    DROP_MAGNET_STATUS_OFF("drop.gui.magnet.status.off", "&cWYŁĄCZONY", true),
    DROP_MAGNET_REQUIRED_LEVEL("drop.gui.magnet.req_level", 10),

    DROP_POLISH_NAMES("drop.polish_names", Arrays.asList("STONE:&7Kamień", "COBBLESTONE:&7Bruk (Brak SilkTouch)", "COAL:&8Węgiel", "IRON_INGOT:&7Sztabka Żelaza", "COPPER_INGOT:&6Sztabka Miedzi", "GOLD_INGOT:&eSztabka Złota", "DIAMOND:&bDiament", "EMERALD:&aSzmaragd", "NETHERITE_SCRAP:&5Odłamek Netheritu"), true),


    //GAME SPAWN
    SAFE_SPAWN_ENABLED("safe_spawn.enabled", false),
    SAFE_SPAWN_WORLD("safe.spawn.world", "spawn"),
    SAFE_SPAWN_X("safe.spawn.x", -43),
    SAFE_SPAWN_Y("safe.spawn.y", 144),
    SAFE_SPAWN_Z("safe.spawn.z", -24),
    SAFE_SPAWN_MINIMUM_Y("safe.spawn.minimum_y", -64),
    SAFE_SPAWN_FACING_X("safe.spawn.facing_x", 90),
    SAFE_SPAWN_FACING_Y("safe.spawn.facing_y", 0),

    //STONE-GENERATOR
    GENERATOR_RECIPE_ENABLED("generator.enabled", true),
    GENERATOR_RECIPE_SHAPE("generator.recipe.shape", Arrays.asList("AAA", "ABA", "AAA")),
    GENERATOR_RECIPE_INGREDIENTS("generator.recipe.ingredients", Arrays.asList("A: STONE", "B: DIAMOND_PICKAXE")),
    GENERATOR_ITEM_MATERIAL("generator.item.material", "END_STONE"),
    GENERATOR_ITEM_NAME("generator.item.name", "&6&l⭐ MAGICZNA STONIARKA ⭐", true),
    GENERATOR_ITEM_LORE("generator.item.lore", Arrays.asList(
            "&8&m-----------------------",
            "&7To urządzenie generuje",
            "&fNieskończone pokłady kamienia.",
            "",
            "&e&lINSTRUKCJA:",
            " &8» &fPostaw na ziemi, aby zacząć.",
            " &8» &fZniszcz kilofem, by odzyskać.",
            "",
            "&c&lUWAGA: &7Działa natychmiastowo!",
            "&8&m-----------------------"
    ), true),
    GENERAOTOR_ITEM_NBT_KEY("generator.item.nbt.key", "SkyBlockPlugin"),
    GENERAOTOR_ITEM_NBT_VALUE("generator.item.nbt.value", "STONE_GENERATOR"),
    GENERATOR_TIME_TO_REGEN("generator.regen.time", 2),

    //ORB
    ORB_COOLDOWN("orb.cooldown", 48),
    ORB_ITEM_NAME("orb.mythicitem", "magicznyOrb"),

    CHAT_COOLDOWN("chat.cooldown", 2000),
    CENZURA("cenzura", Arrays.asList("chuj","chuja", "chujek", "chuju", "chujem", "chujnia",
            "chujowy", "chujowa", "chujowe", "cipa", "cipę", "cipe", "cipą",
            "cipie", "dojebać","dojebac", "dojebie", "dojebał", "dojebal",
            "dojebała", "dojebala", "dojebałem", "dojebalem", "dojebałam",
            "dojebalam", "dojebię", "dojebie", "dopieprzać", "dopieprzac",
            "dopierdalać", "dopierdalac", "dopierdala", "dopierdalał",
            "dopierdalal", "dopierdalała", "dopierdalala", "dopierdoli",
            "dopierdolił", "dopierdolil", "dopierdolę", "dopierdole", "dopierdoli",
            "dopierdalający", "dopierdalajacy", "dopierdolić", "dopierdolic",
            "dupa", "dupie", "dupą", "dupcia", "dupeczka", "dupy", "dupe", "huj",
            "hujek", "hujnia", "huja", "huje", "hujem", "huju", "jebać", "jebac",
            "jebał", "jebal", "jebie", "jebią", "jebia", "jebak", "jebaka", "jebal",
            "jebał", "jebany", "jebane", "jebanka", "jebanko", "jebankiem",
            "jebanymi", "jebana", "jebanym", "jebanej", "jebaną", "jebana",
            "jebani", "jebanych", "jebanymi", "jebcie", "jebiący", "jebiacy",
            "jebiąca", "jebiaca", "jebiącego", "jebiacego", "jebiącej", "jebiacej",
            "jebia", "jebią", "jebie", "jebię", "jebliwy", "jebnąć", "jebnac",
            "jebnąc", "jebnać", "jebnął", "jebnal", "jebną", "jebna", "jebnęła",
            "jebnela", "jebnie", "jebnij", "jebut", "koorwa", "kórwa", "kurestwo",
            "kurew", "kurewski", "kurewska", "kurewskiej", "kurewską", "kurewska",
            "kurewsko", "kurewstwo", "kurwa", "kurwaa", "kurwami", "kurwą", "kurwe",
            "kurwę", "kurwie", "kurwiska", "kurwo", "kurwy", "kurwach", "kurwami",
            "kurewski", "kurwiarz", "kurwiący", "kurwica", "kurwić", "kurwic",
            "kurwidołek", "kurwik", "kurwiki", "kurwiszcze", "kurwiszon",
            "kurwiszona", "kurwiszonem", "kurwiszony", "kutas", "kutasa", "kutasie",
            "kutasem", "kutasy", "kutasów", "kutasow", "kutasach", "kutasami",
            "matkojebca", "matkojebcy", "matkojebcą", "matkojebca", "matkojebcami",
            "matkojebcach", "nabarłożyć", "najebać", "najebac", "najebał",
            "najebal", "najebała", "najebala", "najebane", "najebany", "najebaną",
            "najebana", "najebie", "najebią", "najebia", "naopierdalać",
            "naopierdalac", "naopierdalał", "naopierdalal", "naopierdalała",
            "naopierdalala", "naopierdalała", "napierdalać", "napierdalac",
            "napierdalający", "napierdalajacy", "napierdolić", "napierdolic",
            "nawpierdalać", "nawpierdalac", "nawpierdalał", "nawpierdalal",
            "nawpierdalała", "nawpierdalala", "obsrywać", "obsrywac", "obsrywający",
            "obsrywajacy", "odpieprzać", "odpieprzac", "odpieprzy", "odpieprzył",
            "odpieprzyl", "odpieprzyła", "odpieprzyla", "odpierdalać",
            "odpierdalac", "odpierdol", "odpierdolił", "odpierdolil",
            "odpierdoliła", "odpierdolila", "odpierdoli", "odpierdalający",
            "odpierdalajacy", "odpierdalająca", "odpierdalajaca", "odpierdolić",
            "odpierdolic", "odpierdoli", "odpierdolił", "opieprzający",
            "opierdalać", "opierdalac", "opierdala", "opierdalający",
            "opierdalajacy", "opierdol", "opierdolić", "opierdolic", "opierdoli",
            "opierdolą", "opierdola", "piczka", "pieprznięty", "pieprzniety",
            "pieprzony", "pierdel", "pierdlu", "pierdolą", "pierdola", "pierdolący",
            "pierdolacy", "pierdoląca", "pierdolaca", "pierdol", "pierdole",
            "pierdolenie", "pierdoleniem", "pierdoleniu", "pierdolę", "pierdolec",
            "pierdola", "pierdolą", "pierdolić", "pierdolicie", "pierdolic",
            "pierdolił", "pierdolil", "pierdoliła", "pierdolila", "pierdoli",
            "pierdolnięty", "pierdolniety", "pierdolisz", "pierdolnąć",
            "pierdolnac", "pierdolnął", "pierdolnal", "pierdolnęła", "pierdolnela",
            "pierdolnie", "pierdolnięty", "pierdolnij", "pierdolnik", "pierdolona",
            "pierdolone", "pierdolony", "pierdołki", "pierdzący", "pierdzieć",
            "pierdziec", "pizda", "pizdą", "pizde", "pizdę", "piździe", "pizdzie",
            "pizdnąć", "pizdnac", "pizdu", "podpierdalać", "podpierdalac",
            "podpierdala", "podpierdalający", "podpierdalajacy", "podpierdolić",
            "podpierdolic", "podpierdoli", "pojeb", "pojeba", "pojebami",
            "pojebani", "pojebanego", "pojebanemu", "pojebani", "pojebany",
            "pojebanych", "pojebanym", "pojebanymi", "pojebem", "pojebać",
            "pojebac", "pojebalo", "popierdala", "popierdalac", "popierdalać",
            "popierdolić", "popierdolic", "popierdoli", "popierdolonego",
            "popierdolonemu", "popierdolonym", "popierdolone", "popierdoleni",
            "popierdolony", "porozpierdalać", "porozpierdala", "porozpierdalac",
            "poruchac", "poruchać", "przejebać", "przejebane", "przejebac",
            "przyjebali", "przepierdalać", "przepierdalac", "przepierdala",
            "przepierdalający", "przepierdalajacy", "przepierdalająca",
            "przepierdalajaca", "przepierdolić", "przepierdolic", "przyjebać",
            "przyjebac", "przyjebie", "przyjebała", "przyjebala", "przyjebał",
            "przyjebal", "przypieprzać", "przypieprzac", "przypieprzający",
            "przypieprzajacy", "przypieprzająca", "przypieprzajaca",
            "przypierdalać", "przypierdalac", "przypierdala", "przypierdoli",
            "przypierdalający", "przypierdalajacy", "przypierdolić",
            "przypierdolic", "qrwa", "rozjebać", "rozjebac", "rozjebie",
            "rozjebała", "rozjebią", "rozpierdalać", "rozpierdalac", "rozpierdala",
            "rozpierdolić", "rozpierdolic", "rozpierdole", "rozpierdoli",
            "rozpierducha", "skurwić", "skurwiel", "skurwiela", "skurwielem",
            "skurwielu", "skurwysyn", "skurwysynów", "skurwysynow", "skurwysyna",
            "skurwysynem", "skurwysynu", "skurwysyny", "skurwysyński",
            "skurwysynski", "skurwysyństwo", "skurwysynstwo", "spieprzać",
            "spieprzac", "spieprza", "spieprzaj", "spieprzajcie", "spieprzają",
            "spieprzaja", "spieprzający", "spieprzajacy", "spieprzająca",
            "spieprzajaca", "spierdalać", "spierdalac", "spierdala", "spierdalał",
            "spierdalała", "spierdalal", "spierdalalcie", "spierdalala",
            "spierdalający", "spierdalajacy", "spierdolić", "spierdolic",
            "spierdoli", "spierdoliła", "spierdoliło", "spierdolą", "spierdola",
            "srać", "srac", "srający", "srajacy", "srając", "srajac", "sraj",
            "sukinsyn", "sukinsyny", "sukinsynom", "sukinsynowi", "sukinsynów",
            "sukinsynow", "śmierdziel", "udupić", "ujebać", "ujebac", "ujebał",
            "ujebal", "ujebana", "ujebany", "ujebie", "ujebała", "ujebala",
            "upierdalać", "upierdalac", "upierdala", "upierdoli", "upierdolić",
            "upierdolic", "upierdoli", "upierdolą", "upierdola", "upierdoleni",
            "wjebać", "wjebac", "wjebie", "wjebią", "wjebia", "wjebiemy",
            "wjebiecie", "wkurwiać", "wkurwiac", "wkurwi", "wkurwia", "wkurwiał",
            "wkurwial", "wkurwiający", "wkurwiajacy", "wkurwiająca", "wkurwiajaca",
            "wkurwić", "wkurwic", "wkurwi", "wkurwiacie", "wkurwiają", "wkurwiali",
            "wkurwią", "wkurwia", "wkurwimy", "wkurwicie", "wkurwiacie", "wkurwić",
            "wkurwic", "wkurwia", "wpierdalać", "wpierdalac", "wpierdalający",
            "wpierdalajacy", "wpierdol", "wpierdolić", "wpierdolic", "wpizdu",
            "wyjebać", "wyjebac", "wyjebali", "wyjebał", "wyjebac", "wyjebała",
            "wyjebały", "wyjebie", "wyjebią", "wyjebia", "wyjebiesz", "wyjebie",
            "wyjebiecie", "wyjebiemy", "wypieprzać", "wypieprzac", "wypieprza",
            "wypieprzał", "wypieprzal", "wypieprzała", "wypieprzala", "wypieprzy",
            "wypieprzyła", "wypieprzyla", "wypieprzył", "wypieprzyl", "wypierdal",
            "wypierdalać", "wypierdalac", "wypierdala", "wypierdalaj",
            "wypierdalał", "wypierdalal", "wypierdalała", "wypierdalala",
            "wypierdalać", "wypierdolić", "wypierdolic", "wypierdoli",
            "wypierdolimy", "wypierdolicie", "wypierdolą", "wypierdola",
            "wypierdolili", "wypierdolił", "wypierdolil", "wypierdoliła",
            "wypierdolila", "zajebać", "zajebac", "zajebie", "zajebią", "zajebia",
            "zajebiał", "zajebial", "zajebała", "zajebiala", "zajebali", "zajebana",
            "zajebani", "zajebane", "zajebany", "zajebanych", "zajebanym",
            "zajebanymi", "zajebiste", "zajebisty", "zajebistych", "zajebista",
            "zajebistym", "zajebistymi", "zajebiście", "zajebiscie", "zapieprzyć",
            "zapieprzyc", "zapieprzy", "zapieprzył", "zapieprzyl", "zapieprzyła",
            "zapieprzyla", "zapieprzą", "zapieprza", "zapieprzy", "zapieprzymy",
            "zapieprzycie", "zapieprzysz", "zapierdala", "zapierdalać",
            "zapierdalac", "zapierdalaja", "zapierdalał", "zapierdalaj",
            "zapierdalajcie", "zapierdalała", "zapierdalala", "zapierdalali",
            "zapierdalający", "zapierdalajacy", "zapierdolić", "zapierdolic",
            "zapierdoli", "zapierdolił", "zapierdolil", "zapierdoliła",
            "zapierdolila", "zapierdolą", "zapierdola", "zapierniczać",
            "zapierniczający", "zasrać", "zasranym", "zasrywać", "zasrywający",
            "zesrywać", "zesrywający", "zjebać", "zjebac", "zjebał", "zjebal",
            "zjebała", "zjebala", "zjebana", "zjebią", "zjebali", "zjeby")),
    CENZURA_REPLACE("cenzura_replace", Arrays.asList("*uwu*", "*meow*"))

    ;



    private final String path;
    private List<String> texts;
    private String text;
    private Component component;
    private List<Component> components;
    private int number;
    private boolean bool;
    private boolean colored = false;
    private Class<?> type;


    private Config(String path, String text) {
        this(path, text, false);
    }
    private Config(String path, String text, boolean colored) {
        this.path = path;
        this.colored = colored;
        setValue(text);
    }
    private Config(String path, List<String> texts) {
        this(path, texts, false);
    }
    private Config(String path, List<String> texts, boolean colored) {
        this.path = path;
        this.colored = colored;
        setValue(texts);
    }
    private Config(String path, int number) {
        this.path = path;
        setValue(number);
    }
    private Config(String path, boolean bool) {
        this.path = path;
        setValue(bool);
    }


    public void setValue(String text) {
        this.type = String.class;
        this.text = text;
        this.component = this.colored? parseComponent(this.text) : null;
        this.texts = Collections.unmodifiableList(Arrays.asList(this.text));
        this.components = this.colored? Collections.unmodifiableList(Arrays.asList(parseComponent(this.text))) : null;
        this.number = text.length();
        this.bool = !text.isEmpty();
    }
    public void setValue(List<String> texts) {
        this.type = String[].class;
        this.text = String.join(", ", texts);
        this.component = this.colored? parseComponent(this.text) : null;
        this.texts = Collections.unmodifiableList(texts);
        this.components = this.colored? Collections.unmodifiableList(this.texts.stream().map(Config::parseComponent).collect(Collectors.toList())) : null;
        this.number = texts.size();
        this.bool = !texts.isEmpty();
    }
    public void setValue(int number) {
        this.type = Integer.class;
        this.text = Integer.toString(number);
        this.component = null;
        this.texts = Collections.unmodifiableList(Arrays.asList(this.text));
        this.components = null;
        this.number = number;
        this.bool = number > 0;
    }
    public void setValue(boolean bool) {
        this.type = Boolean.class;
        this.text = Boolean.toString(bool);
        this.component = null;
        this.texts = Collections.unmodifiableList(Arrays.asList(this.text));
        this.components = null;
        this.number = bool? 1 : 0;
        this.bool = bool;
    }

    public static Component parseComponent(@NotNull String text) {
        try {
            return (Component) GsonComponentSerializer.gson().deserialize(text);
        } catch (Exception e) {
            return (Component) LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(text);
        }
    }


    public String getString() {
        if (this == MAIN_PREFIX) {
            return this.text;
        }

        return this.text.replace("<prefix>", MAIN_PREFIX.text);
    }
    @Override
    public String toString() {
        return this.text;
    }
    public List<String> getStringList() {
        return this.texts;
    }
    public int getInt() {
        return this.number;
    }
    public boolean getBoolean() {
        return this.bool;
    }
    public File getFile() {
        return new File(this.text);
    }
    public Component getComponent() {
        return this.component;
    }
    public boolean isColored() {
        return this.colored;
    }
    public String getPath() {
        return this.path;
    }
    public Class<?> getType() {
        return this.type;
    }

    public static void load(File file) {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if(!file.exists()) file.createNewFile();
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            if(loadConfig(yml) > 0) yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void save(File file) {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if(!file.exists()) file.createNewFile();
            saveConfig().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static YamlConfiguration saveConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (Config val : Config.values()) {
            if (val.getType().equals(String.class)) {
                if (val.isColored()) config.set(val.getPath(), val.getString().replace('§', '&'));
                else config.set(val.getPath(), val.getString());
            } else if (val.getType().equals(String[].class)) {
                if (val.isColored()) config.set(val.getPath(), val.getStringList().stream().map(s -> s.replace('§', '&')).collect(Collectors.toList()));
                else config.set(val.getPath(), val.getStringList());
            } else if (val.getType().equals(Integer.class)) config.set(val.getPath(), val.getInt());
            else if (val.getType().equals(Boolean.class)) config.set(val.getPath(), val.getBoolean());
        }
        return config;
    }
    private static int loadConfig(ConfigurationSection config) {
        int modify = 0;
        for (Config val : Config.values()) {
            if(!config.contains(val.getPath())) modify++;
            if (val.getType().equals(String.class)) {
                if (val.isColored())val.setValue(getColoredStringOrSetDefault(config, val.getPath(), val.getString()));
                else val.setValue(getStringOrSetDefault(config, val.getPath(), val.getString()));
            } else if (val.getType().equals(String[].class)) {
                if (val.isColored())val.setValue(getColoredStringListOrSetDefault(config, val.getPath(), val.getStringList()));
                else val.setValue(getStringListOrSetDefault(config, val.getPath(), val.getStringList()));
            } else if (val.getType().equals(Integer.class)) val.setValue(getIntOrSetDefault(config, val.getPath(), val.getInt()));
            else if (val.getType().equals(Boolean.class)) val.setValue(getBooleanOrSetDefault(config, val.getPath(), val.getBoolean()));
        }
        return modify;
    }



    private static int getIntOrSetDefault(ConfigurationSection config, String path, int def) {
        if (config.contains(path)) return config.getInt(path);
        config.set(path, def);
        return def;
    }
    private static boolean getBooleanOrSetDefault(ConfigurationSection config, String path, boolean def) {
        if (config.contains(path)) return config.getBoolean(path);
        config.set(path, def);
        return def;
    }
    private static String getStringOrSetDefault(ConfigurationSection config, String path, String def) {
        if (config.contains(path)) return config.getString(path);
        config.set(path, def);
        return def;
    }
    private static String getColoredStringOrSetDefault(ConfigurationSection config, String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', getStringOrSetDefault(config, path, def.replace('§', '&')));
    }
    private static ArrayList<String> getStringListOrSetDefault(ConfigurationSection config, String path, List<String> def) {
        if(config.contains(path)) return new ArrayList<>(config.getStringList(path));
        config.set(path, def);
        return new ArrayList<>(def);
    }
    private static ArrayList<String> getColoredStringListOrSetDefault(ConfigurationSection config, String path, List<String> def) {
        ArrayList<String> list = getStringListOrSetDefault(config, path, def.stream().map(str -> str.replace('§', '&')).collect(Collectors.toCollection(ArrayList::new)));
        return list.stream().map(str -> ChatColor.translateAlternateColorCodes('&', str))
                .collect(Collectors.toCollection(ArrayList::new));
    }



}
