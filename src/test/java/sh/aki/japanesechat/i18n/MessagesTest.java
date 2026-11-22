package sh.aki.japanesechat.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {

    private static final Pattern ARG = Pattern.compile("\\{(\\d)}");

    @Test
    void bundlesHaveTheSameKeysAndArguments() throws IOException {
        Map<String, String> en = Messages.bundled("en");
        for (String lang : Messages.BUNDLED) {
            Map<String, String> other = Messages.bundled(lang);
            assertEquals(new TreeSet<>(en.keySet()), new TreeSet<>(other.keySet()), lang);
            for (String key : en.keySet()) {
                assertEquals(args(en.get(key)), args(other.get(key)), lang + " " + key);
            }
        }
    }

    @Test
    void everyKeyUsedInCodeExists() throws IOException {
        Map<String, String> en = Messages.bundled("en");
        Pattern literal = Pattern.compile("\"((?:error|state|status|convert|view|test|reload|usage|help)\\.[a-z.-]+)\"");
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            // config paths look like message keys, so the config code and the main class are skipped
            for (Path f : files.filter(p -> p.toString().endsWith(".java") && !p.toString().contains("/config/")
                    && !p.endsWith("JapaneseChatPlugin.java")).toList()) {
                Matcher m = literal.matcher(Files.readString(f));
                while (m.find()) {
                    String key = m.group(1);
                    if (!key.endsWith(".")) {
                        assertTrue(en.containsKey(key), f.getFileName() + ": " + key);
                    }
                }
            }
        }
    }

    @Test
    void argumentsCannotInjectTags() {
        Component c = Messages.render("<red>Hi {0}", "<green>evil");
        assertEquals("Hi <green>evil", PlainTextComponentSerializer.plainText().serialize(c));
    }

    @Test
    void componentArgumentsKeepTheirStyle() {
        Component c = Messages.render("<gray>to {0}", Component.text("Lobby", NamedTextColor.GREEN));
        assertEquals("to Lobby", PlainTextComponentSerializer.plainText().serialize(c));
        assertTrue(c.toString().contains("green"));
    }

    private static TreeSet<String> args(String template) {
        TreeSet<String> s = new TreeSet<>();
        Matcher m = ARG.matcher(template);
        while (m.find()) {
            s.add(m.group(1));
        }
        return s;
    }
}
