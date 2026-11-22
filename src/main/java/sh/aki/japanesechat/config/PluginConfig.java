package sh.aki.japanesechat.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public record PluginConfig(String language, boolean kanji, int timeoutMs, String format, Defaults defaults,
                           List<String> warnings) {

    public static final int CURRENT_VERSION = 2;
    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");

    public record Defaults(boolean convert, boolean view) {
    }

    public static PluginConfig parse(ConfigurationSection c) {
        List<String> warnings = new ArrayList<>();
        String format = c.getString("format", " <gray>(<converted>)");
        if (!format.contains("<converted>")) {
            warnings.add("format does not contain <converted>; the conversion would not be shown");
        }
        int timeout = c.getInt("kanji.timeout-ms", 2000);
        if (timeout < 200 || timeout > 10_000) {
            warnings.add("kanji.timeout-ms must be 200-10000, got " + timeout);
            timeout = Math.max(200, Math.min(timeout, 10_000));
        }
        return new PluginConfig(c.getString("language", "auto"), c.getBoolean("kanji.enabled", true), timeout,
                format, new Defaults(c.getBoolean("defaults.convert", true), c.getBoolean("defaults.view", true)),
                List.copyOf(warnings));
    }

    public static boolean isLegacy(ConfigurationSection c) {
        return !c.contains("config-version")
                && (c.contains("converted-text-color") || c.contains("default-toggle-state"));
    }

    /** Moves the 1.x settings into a fresh copy of the bundled config. */
    public static YamlConfiguration migrate(ConfigurationSection old, YamlConfiguration defaults) {
        defaults.set("config-version", CURRENT_VERSION);
        String color = old.getString("converted-text-color", "gray").trim();
        String tag = HEX.matcher(color).matches() ? color : color.toLowerCase(Locale.ROOT).replace(' ', '_');
        if (!tag.matches("#[0-9a-fA-F]{6}|[a-z_]+")) {
            tag = "gray";
        }
        defaults.set("format", " <" + tag + ">(<converted>)");
        if (old.contains("default-toggle-state")) {
            defaults.set("defaults.view", old.getBoolean("default-toggle-state"));
        }
        return defaults;
    }
}
