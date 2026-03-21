package me.aki.japanizeChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JapanizeCommand implements CommandExecutor, TabCompleter {
    private final JapanizeChat plugin;

    public JapanizeCommand(JapanizeChat plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            UUID uuid = player.getUniqueId();
            boolean newStateIsOff;
            if (plugin.getToggleOffPlayers().contains(uuid)) {
                plugin.getToggleOffPlayers().remove(uuid);
                newStateIsOff = false;
                player.sendMessage(Component.text("変換表示を【オン】にしました。", NamedTextColor.GREEN));
            } else {
                plugin.getToggleOffPlayers().add(uuid);
                newStateIsOff = true;
                player.sendMessage(Component.text("変換表示を【オフ】にしました。", NamedTextColor.YELLOW));
            }
            plugin.savePlayerSettings(uuid, newStateIsOff);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && player.hasPermission("japanizechat.admin")) {
            plugin.loadConfigValues();
            player.sendMessage(Component.text("設定を再読み込みしました。", NamedTextColor.AQUA));
            return true;
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("--- JapanizeChat 使い方 ---", NamedTextColor.AQUA));
        player.sendMessage(Component.text("/jp toggle : 自分の変換表示を切り替え", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/jp reload : 全体設定のリロード", NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList("toggle", "help", "reload");
        return Collections.emptyList();
    }
}
