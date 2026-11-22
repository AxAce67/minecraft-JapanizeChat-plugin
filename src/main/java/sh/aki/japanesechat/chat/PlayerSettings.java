package sh.aki.japanesechat.chat;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import sh.aki.japanesechat.config.PluginConfig;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Per-player choices, stored with the player data. Chat runs off the main thread, so it reads a
 * copy kept here instead of the player's data container.
 */
public final class PlayerSettings implements Listener {

    public record Choice(boolean convert, boolean view) {
    }

    private final Plugin plugin;
    private final Supplier<PluginConfig> config;
    private final NamespacedKey convertKey;
    private final NamespacedKey viewKey;
    private final Map<UUID, Choice> online = new ConcurrentHashMap<>();
    private final Set<String> names = ConcurrentHashMap.newKeySet();

    public PlayerSettings(Plugin plugin, Supplier<PluginConfig> config) {
        this.plugin = plugin;
        this.config = config;
        this.convertKey = new NamespacedKey(plugin, "convert");
        this.viewKey = new NamespacedKey(plugin, "view");
    }

    public Choice get(UUID player) {
        Choice c = online.get(player);
        return c != null ? c : defaults();
    }

    private Choice defaults() {
        PluginConfig.Defaults d = config.get().defaults();
        return new Choice(d.convert(), d.view());
    }

    /** Online player names in lower case, safe to read from the chat thread. */
    public boolean isPlayerName(String lowerCase) {
        return names.contains(lowerCase);
    }

    public void load(Player player) {
        names.add(player.getName().toLowerCase(Locale.ROOT));
        migrateOldFile(player);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Choice d = defaults();
        online.put(player.getUniqueId(), new Choice(read(pdc, convertKey, d.convert()), read(pdc, viewKey, d.view())));
    }

    public void setConvert(Player player, boolean on) {
        player.getPersistentDataContainer().set(convertKey, PersistentDataType.BYTE, (byte) (on ? 1 : 0));
        load(player);
    }

    public void setView(Player player, boolean on) {
        player.getPersistentDataContainer().set(viewKey, PersistentDataType.BYTE, (byte) (on ? 1 : 0));
        load(player);
    }

    private static boolean read(PersistentDataContainer pdc, NamespacedKey key, boolean fallback) {
        Byte b = pdc.get(key, PersistentDataType.BYTE);
        return b == null ? fallback : b != 0;
    }

    /** 1.x kept "conversion-off" (hide other players' conversions) in players/<uuid>.yml. */
    private void migrateOldFile(Player player) {
        File file = new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
        if (!file.isFile()) {
            return;
        }
        if (YamlConfiguration.loadConfiguration(file).getBoolean("conversion-off", false)) {
            player.getPersistentDataContainer().set(viewKey, PersistentDataType.BYTE, (byte) 0);
        }
        if (!file.delete()) {
            plugin.getLogger().warning("Could not delete " + file);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        online.remove(event.getPlayer().getUniqueId());
        names.remove(event.getPlayer().getName().toLowerCase(Locale.ROOT));
    }
}
