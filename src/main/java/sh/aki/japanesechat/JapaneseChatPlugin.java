package sh.aki.japanesechat;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sh.aki.japanesechat.chat.ChatListener;
import sh.aki.japanesechat.chat.PlayerSettings;
import sh.aki.japanesechat.command.JpCommand;
import sh.aki.japanesechat.config.PluginConfig;
import sh.aki.japanesechat.convert.GoogleInputTools;
import sh.aki.japanesechat.i18n.Messages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Objects;

public final class JapaneseChatPlugin extends JavaPlugin {

    public static final String USE = "japanesechat.use";
    public static final String ADMIN = "japanesechat.admin";

    private volatile PluginConfig config;
    private volatile Messages messages;
    private GoogleInputTools kanji;
    private PlayerSettings settings;
    private ChatListener chat;

    @Override
    public void onEnable() {
        kanji = new GoogleInputTools(Duration.ofMillis(2000));
        moveOldDataFolder();
        if (!load()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        settings = new PlayerSettings(this, this::config);
        chat = new ChatListener(this);
        getServer().getPluginManager().registerEvents(settings, this);
        getServer().getPluginManager().registerEvents(chat, this);
        getServer().getOnlinePlayers().forEach(settings::load);

        JpCommand command = new JpCommand(this);
        PluginCommand jp = Objects.requireNonNull(getCommand("jp"));
        jp.setExecutor(command);
        jp.setTabCompleter(command);
    }

    /** Reloads config and messages. On failure the previous state is kept. */
    public boolean load() {
        try {
            saveDefaultConfig();
            migrateIfNeeded();
            reloadConfig();
            PluginConfig parsed = PluginConfig.parse(getConfig());
            Messages loaded = Messages.load(parsed.language(), getDataFolder().toPath().resolve("lang"));
            parsed.warnings().forEach(w -> getLogger().warning("config.yml: " + w));
            config = parsed;
            messages = loaded;
            kanji.setTimeout(Duration.ofMillis(parsed.timeoutMs()));
            return true;
        } catch (IOException | RuntimeException e) {
            getLogger().severe("Could not load the configuration: " + e);
            return config != null;
        }
    }

    /** 1.x was called JapanizeChat and kept its files in plugins/JapanizeChat. */
    private void moveOldDataFolder() {
        File old = new File(getDataFolder().getParentFile(), "JapanizeChat");
        if (old.isDirectory() && !getDataFolder().exists()) {
            if (old.renameTo(getDataFolder())) {
                getLogger().info("Moved plugins/JapanizeChat to plugins/" + getDataFolder().getName());
            } else {
                getLogger().warning("Could not move plugins/JapanizeChat; copy its files by hand.");
            }
        }
    }

    private void migrateIfNeeded() throws IOException {
        File file = new File(getDataFolder(), "config.yml");
        YamlConfiguration current = YamlConfiguration.loadConfiguration(file);
        if (!PluginConfig.isLegacy(current)) {
            return;
        }
        File backup = new File(getDataFolder(), "config-1.x.yml");
        Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        YamlConfiguration defaults;
        try (InputStream in = Objects.requireNonNull(getResource("config.yml"))) {
            defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        PluginConfig.migrate(current, defaults).save(file);
        getLogger().info("Converted config.yml from 1.x. The old file was saved as " + backup.getName());
    }

    public PluginConfig config() {
        return config;
    }

    public Messages messages() {
        return messages;
    }

    public GoogleInputTools kanji() {
        return kanji;
    }

    public PlayerSettings settings() {
        return settings;
    }

    public ChatListener chat() {
        return chat;
    }
}
