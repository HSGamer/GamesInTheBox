package me.hsgamer.gamesinthebox.util;

import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.gamesinthebox.feature.TopFeature;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.GameState;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public final class ArenaUtils {
    private ArenaUtils() {
        // EMPTY
    }

    public static Pair<String, Integer> parseSelector(String selector) {
        String[] split = selector.split(":", 2);
        String arenaName = split[0];
        int index = split.length > 1 ? Integer.parseInt(split[1]) : 0;
        return Pair.of(arenaName, index);
    }

    public static String getStateName(Arena arena) {
        return arena.getStateInstance()
                .map(GameState::getDisplayName)
                .map(MessageUtils::colorize)
                .orElse("");
    }

    public static String getCooldown(Arena arena) {
        return arena.getArenaFeature(CooldownFeature.class).getCooldownFormat();
    }

    public static String getCurrentGame(Arena arena) {
        return Optional.ofNullable(arena.getArenaFeature(GameFeature.class).getCurrentGame())
                .map(arenaGame -> arenaGame.replace(arenaGame.getDisplayName()))
                .map(MessageUtils::colorize)
                .orElse("");
    }

    public static Optional<String> getTopName(Arena arena, int index) {
        return arena.getArenaFeature(TopFeature.class)
                .getTop(index)
                .map(Pair::getKey)
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName);
    }

    public static Optional<String> getTopValue(Arena arena, int index) {
        return arena.getArenaFeature(TopFeature.class)
                .getTop(index)
                .map(Pair::getValue);
    }
}
