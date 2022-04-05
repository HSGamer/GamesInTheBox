package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.builder.ArenaGameBuilder;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class GameFeature extends ArenaFeature<GameFeature.ArenaGameFeature> {

    @Override
    public boolean isArenaSupported(Arena arena) {
        return arena.getFeature(ConfigFeature.class).contains(arena, "settings");
    }

    @Override
    protected ArenaGameFeature createFeature(Arena arena) {
        return new ArenaGameFeature(arena);
    }

    public static class ArenaGameFeature implements Feature {
        private final Arena arena;
        private final Map<String, ArenaGame> games;
        private final AtomicReference<ArenaGame> currentGame;

        public ArenaGameFeature(Arena arena) {
            this.arena = arena;
            this.games = new HashMap<>();
            this.currentGame = new AtomicReference<>();
        }

        @Override
        public void init() {
            arena.getArenaFeature(ConfigFeature.class).getValues("settings", false).forEach((key, value) -> {
                if (!(value instanceof Map)) {
                    return;
                }
                //noinspection unchecked
                Map<String, Object> map = (Map<String, Object>) value;
                if (!map.containsKey("type")) {
                    return;
                }
                String type = Objects.toString(map.get("type"));
                try {
                    ArenaGameBuilder.INSTANCE.build(type, Pair.of(arena, key)).ifPresent(game -> {
                        game.init();
                        games.put(key, game);
                    });
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, e, () -> "Failed to load game " + key + " in arena " + arena.getName());
                }
            });
        }

        public boolean isGameExist(String name) {
            return games.containsKey(name);
        }

        public ArenaGame getCurrentGame() {
            return currentGame.get();
        }

        public void setCurrentGame(String game) {
            if (games.containsKey(game)) {
                currentGame.set(games.get(game));
            }
        }

        public List<String> getAvailableGames() {
            return new ArrayList<>(games.keySet());
        }

        @Override
        public void clear() {
            games.values().forEach(ArenaGame::clear);
            currentGame.set(null);
        }
    }
}
