package sh.aki.japanesechat.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * MiniMessage templates from {@code lang/messages_<lang>.properties}. Files with the same name in the
 * plugin folder override single keys. {@code {0}} style arguments are inserted as placeholders, so
 * plain strings cannot inject tags and components keep their formatting.
 */
public final class Messages {

    public static final String FALLBACK = "en";
    public static final List<String> BUNDLED = List.of("en", "ja");
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Map<String, Map<String, String>> tables;
    private final String language;

    private Messages(Map<String, Map<String, String>> tables, String language) {
        this.tables = tables;
        this.language = language;
    }

    /** @param language {@code auto} (each player's client language), or a language code */
    public static Messages load(String language, Path overrideDir) throws IOException {
        Map<String, Map<String, String>> tables = new HashMap<>();
        for (String lang : BUNDLED) {
            Map<String, String> table = new HashMap<>(bundled(lang));
            Path override = overrideDir.resolve("messages_" + lang + ".properties");
            if (Files.isRegularFile(override)) {
                try (Reader r = Files.newBufferedReader(override, StandardCharsets.UTF_8)) {
                    table.putAll(read(r));
                }
            }
            tables.put(lang, table);
        }
        String lang = language == null ? "auto" : language.trim().toLowerCase(Locale.ROOT);
        return new Messages(tables, lang);
    }

    public static Map<String, String> bundled(String lang) throws IOException {
        String name = lang.equals(FALLBACK) ? "lang/messages.properties" : "lang/messages_" + lang + ".properties";
        try (InputStream in = Messages.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("missing " + name);
            }
            return read(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    private static Map<String, String> read(Reader r) throws IOException {
        Properties p = new Properties();
        p.load(r);
        Map<String, String> m = new HashMap<>();
        p.forEach((k, v) -> m.put(k.toString(), v.toString()));
        return m;
    }

    public String languageOf(CommandSender sender) {
        String lang = language;
        if (lang.equals("auto")) {
            lang = sender instanceof Player p ? p.locale().getLanguage() : Locale.getDefault().getLanguage();
        }
        return tables.containsKey(lang) ? lang : FALLBACK;
    }

    public String raw(String lang, String key) {
        String v = tables.getOrDefault(lang, Map.of()).get(key);
        if (v == null) {
            v = tables.get(FALLBACK).get(key);
        }
        return v == null ? key : v;
    }

    public Component get(CommandSender to, String key, Object... args) {
        return render(raw(languageOf(to), key), args);
    }

    public void send(CommandSender to, String key, Object... args) {
        to.sendMessage(get(to, key, args));
    }

    static Component render(String template, Object... args) {
        if (args.length == 0) {
            return MM.deserialize(template);
        }
        TagResolver.Builder resolvers = TagResolver.builder();
        for (int i = 0; i < args.length; i++) {
            String tag = "arg" + i;
            template = template.replace("{" + i + "}", "<" + tag + ">");
            resolvers.resolver(args[i] instanceof Component c
                    ? Placeholder.component(tag, c)
                    : Placeholder.unparsed(tag, String.valueOf(args[i])));
        }
        return MM.deserialize(template, resolvers.build());
    }
}
