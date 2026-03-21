package me.aki.japanizeChat;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class JapanizeChat extends JavaPlugin implements Listener {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Set<UUID> toggleOffPlayers = new HashSet<>();
    private TextColor convertedColor;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        File playersFolder = new File(getDataFolder(), "players");
        if (!playersFolder.exists()) playersFolder.mkdirs();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("jp").setExecutor(new JapanizeCommand(this));
    }

    public void loadConfigValues() {
        reloadConfig();
        String colorName = getConfig().getString("converted-text-color", "gray");
        TextColor parsedColor = NamedTextColor.NAMES.value(colorName.toLowerCase());
        if (parsedColor == null) parsedColor = TextColor.fromHexString(colorName);
        this.convertedColor = (parsedColor != null) ? parsedColor : NamedTextColor.GRAY;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File playerFile = new File(getDataFolder(), "players/" + player.getUniqueId() + ".yml");

        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            boolean isOff = config.getBoolean("conversion-off", false);
            if (isOff) toggleOffPlayers.add(player.getUniqueId());
        }
    }

    public void savePlayerSettings(UUID uuid, boolean isOff) {
        File playerFile = new File(getDataFolder(), "players/" + uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("conversion-off", isOff);
        try {
            config.save(playerFile);
        } catch (IOException e) {
            getLogger().warning("プレイヤーデータの保存に失敗しました: " + uuid);
        }
    }

    public Set<UUID> getToggleOffPlayers() { return toggleOffPlayers; }
    public TextColor getConvertedColor() { return convertedColor; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        String original = PlainTextComponentSerializer.plainText().serialize(event.originalMessage()).trim();
        if (!original.matches("^[\\x20-\\x7E\\s]+$") || original.length() < 2) return;

        String hiragana = RomajiToKana.convertToKana(original);
        String converted = fetchGoogleTrans(hiragana);

        if (converted != null && !converted.equalsIgnoreCase(original)) {
            event.renderer((source, sourceDisplayName, message, viewer) -> {
                Component baseChat = ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);
                if (viewer instanceof Player viewerPlayer && toggleOffPlayers.contains(viewerPlayer.getUniqueId())) {
                    return baseChat;
                }
                return baseChat.append(Component.text(" (" + converted + ")", this.convertedColor));
            });
        }
    }

    private String fetchGoogleTrans(String kanaText) {
        try {
            String url = "https://inputtools.google.com/request?text=" + URLEncoder.encode(kanaText, StandardCharsets.UTF_8) + "&itc=ja-t-i0-und&num=1";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "Mozilla/5.0").GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseGoogleJson(response.body());
        } catch (Exception e) { return null; }
    }

    private String parseGoogleJson(String json) {
        try {
            JsonArray root = JsonParser.parseString(json).getAsJsonArray();
            JsonArray results = root.get(1).getAsJsonArray();
            return results.get(0).getAsJsonArray().get(1).getAsJsonArray().get(0).getAsString();
        } catch (Exception e) { return null; }
    }
}
