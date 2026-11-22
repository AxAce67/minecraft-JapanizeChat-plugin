package sh.aki.japanesechat.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sh.aki.japanesechat.JapaneseChatPlugin;
import sh.aki.japanesechat.chat.PlayerSettings;
import sh.aki.japanesechat.i18n.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** {@code /jp}: per-player switches, a conversion test and reload. */
public final class JpCommand implements TabExecutor {

    private final JapaneseChatPlugin plugin;

    public JpCommand(JapaneseChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages m = plugin.messages();
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "" -> {
                status(sender);
                help(sender);
            }
            case "help" -> help(sender);
            case "on", "off" -> {
                if (sender instanceof Player p) {
                    plugin.settings().setConvert(p, sub.equals("on"));
                    m.send(p, sub.equals("on") ? "convert.on" : "convert.off");
                } else {
                    m.send(sender, "error.player-only");
                }
            }
            case "view" -> {
                if (!(sender instanceof Player p)) {
                    m.send(sender, "error.player-only");
                    return true;
                }
                boolean on = args.length >= 2
                        ? args[1].equalsIgnoreCase("on")
                        : !plugin.settings().get(p.getUniqueId()).view();
                plugin.settings().setView(p, on);
                m.send(p, on ? "view.on" : "view.off");
            }
            case "test" -> {
                if (!sender.hasPermission(JapaneseChatPlugin.ADMIN)) {
                    m.send(sender, "error.no-permission");
                } else if (args.length < 2) {
                    m.send(sender, "usage.test");
                } else {
                    test(sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                }
            }
            case "reload" -> {
                if (!sender.hasPermission(JapaneseChatPlugin.ADMIN)) {
                    m.send(sender, "error.no-permission");
                    return true;
                }
                plugin.load();
                if (plugin.config().warnings().isEmpty()) {
                    plugin.messages().send(sender, "reload.done");
                } else {
                    plugin.messages().send(sender, "reload.warnings", plugin.config().warnings().size());
                }
            }
            default -> m.send(sender, "error.unknown", args[0]);
        }
        return true;
    }

    private void status(CommandSender sender) {
        if (sender instanceof Player p) {
            PlayerSettings.Choice c = plugin.settings().get(p.getUniqueId());
            Messages m = plugin.messages();
            m.send(p, "status", m.get(p, c.convert() ? "state.on" : "state.off"),
                    m.get(p, c.view() ? "state.on" : "state.off"));
        }
    }

    private void help(CommandSender sender) {
        Messages m = plugin.messages();
        m.send(sender, "help.header", version());
        List.of("on", "off", "view").forEach(k -> m.send(sender, "help." + k));
        if (sender.hasPermission(JapaneseChatPlugin.ADMIN)) {
            List.of("test", "reload").forEach(k -> m.send(sender, "help." + k));
        }
    }

    @SuppressWarnings("deprecation") // getPluginMeta() only exists from 1.19.3
    private String version() {
        return plugin.getDescription().getVersion();
    }

    private void test(CommandSender sender, String text) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            long start = System.nanoTime();
            var result = plugin.chat().convert(text, true);
            long ms = (System.nanoTime() - start) / 1_000_000;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (result.isEmpty()) {
                    plugin.messages().send(sender, "test.none", text);
                } else {
                    plugin.messages().send(sender, "test.result", text, result.get(), ms);
                }
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("on", "off", "view", "help"));
            if (sender.hasPermission(JapaneseChatPlugin.ADMIN)) {
                options.addAll(List.of("test", "reload"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            options.addAll(List.of("on", "off"));
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.startsWith(prefix)).toList();
    }
}
