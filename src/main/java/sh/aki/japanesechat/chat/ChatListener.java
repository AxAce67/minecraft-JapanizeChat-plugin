package sh.aki.japanesechat.chat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import sh.aki.japanesechat.JapaneseChatPlugin;
import sh.aki.japanesechat.config.PluginConfig;
import sh.aki.japanesechat.convert.Japanizer;

import java.util.Optional;

/**
 * Appends the conversion to chat messages. The existing renderer is wrapped, not replaced, so chat
 * formats from other plugins stay intact, and each viewer can hide conversions.
 */
public final class ChatListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final JapaneseChatPlugin plugin;

    public ChatListener(JapaneseChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (!sender.hasPermission(JapaneseChatPlugin.USE) || !plugin.settings().get(sender.getUniqueId()).convert()) {
            return;
        }
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        // Kanji needs a web request; a synchronous chat event (sent by a plugin) must not wait for it.
        Optional<String> converted = convert(text, event.isAsynchronous());
        if (converted.isEmpty()) {
            return;
        }
        Component suffix = MM.deserialize(plugin.config().format(), Placeholder.unparsed("converted", converted.get()));
        ChatRenderer previous = event.renderer();
        event.renderer((source, name, message, viewer) -> {
            boolean show = !(viewer instanceof Player p) || plugin.settings().get(p.getUniqueId()).view();
            return previous.render(source, name, show ? message.append(suffix) : message, viewer);
        });
    }

    /** Kana, then kanji if enabled and allowed. Empty when the text is not romaji. */
    public Optional<String> convert(String text, boolean mayBlock) {
        Optional<Japanizer.Result> result = Japanizer.convert(text, plugin.settings()::isPlayerName);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        PluginConfig config = plugin.config();
        if (config.kanji() && mayBlock) {
            return Optional.of(result.get().join(kana -> plugin.kanji().convert(kana).orElse(kana)));
        }
        return Optional.of(result.get().kana());
    }
}
