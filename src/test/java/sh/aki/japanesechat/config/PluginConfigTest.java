package sh.aki.japanesechat.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginConfigTest {

    static YamlConfiguration bundled() throws IOException {
        try (InputStream in = PluginConfigTest.class.getClassLoader().getResourceAsStream("config.yml")) {
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    static YamlConfiguration yaml(String s) throws Exception {
        YamlConfiguration c = new YamlConfiguration();
        c.loadFromString(s);
        return c;
    }

    @Test
    void bundledConfigLoadsCleanly() throws IOException {
        PluginConfig c = PluginConfig.parse(bundled());
        assertEquals(List.of(), c.warnings());
        assertTrue(c.kanji());
        assertEquals(" <gray>(<converted>)", c.format());
        assertTrue(c.defaults().convert());
        assertFalse(PluginConfig.isLegacy(bundled()));
    }

    @Test
    void migratesTheOldConfig() throws Exception {
        YamlConfiguration old = yaml("converted-text-color: \"#55FFFF\"\ndefault-toggle-state: false\n");
        assertTrue(PluginConfig.isLegacy(old));
        PluginConfig c = PluginConfig.parse(yaml(PluginConfig.migrate(old, bundled()).saveToString()));
        assertEquals(" <#55FFFF>(<converted>)", c.format());
        assertFalse(c.defaults().view());
        assertTrue(c.defaults().convert());
        assertEquals(List.of(), c.warnings());

        PluginConfig named = PluginConfig.parse(PluginConfig.migrate(yaml("converted-text-color: Light Purple"), bundled()));
        assertEquals(" <light_purple>(<converted>)", named.format());
    }

    @Test
    void warnsAboutBadValues() throws Exception {
        PluginConfig c = PluginConfig.parse(yaml("format: \"<gray>!\"\nkanji: {timeout-ms: 50}"));
        assertEquals(2, c.warnings().size());
        assertEquals(200, c.timeoutMs());
    }
}
