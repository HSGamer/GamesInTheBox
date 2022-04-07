package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.builder.ArenaGameBuilder;
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
            ArenaGameBuilder.INSTANCE.build(arena).forEach(game -> {
                try {
                    game.init();
                    games.put(game.getName(), game);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, e, () -> "Failed to load game " + game.getName() + " in arena " + arena.getName());
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

        public Optional<ArenaGame> getGame(String name) {
            return Optional.ofNullable(games.get(name));
        }

        @Override
        public void clear() {
            games.values().forEach(ArenaGame::clear);
            currentGame.set(null);
        }
    }
}
