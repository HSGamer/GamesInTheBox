package me.hsgamer.gamesinthebox.util;

import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ActionBarUtils {
    private ActionBarUtils() {
        // EMPTY
    }

    public static void sendActionBar(UUID uuid, String message) {
        message = MessageUtils.colorize(message);
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
