package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.api.editor.EditorFeatureResponse;
import me.hsgamer.gamesinthebox.builder.ArenaGameBuilder;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.base.Feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EditorFeature extends ArenaFeature<EditorFeature.ArenaEditorFeature> {
    private final Map<String, Arena> editingArenas = new HashMap<>();

    @Override
    protected ArenaEditorFeature createFeature(Arena arena) {
        editingArenas.put(arena.getName(), arena);
        return new ArenaEditorFeature(arena);
    }

    public List<String> getArenaNames() {
        return List.copyOf(editingArenas.keySet());
    }

    public void addArena(Arena arena) {
        editingArenas.put(arena.getName(), arena);
        ArenaEditorFeature feature = getFeature(arena);
        ArenaGameBuilder.INSTANCE.build(arena).forEach(game -> feature.editingGames.put(game.getName(), game));
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(editingArenas.get(name));
    }

    public static class ArenaEditorFeature implements Feature {
        private final Arena arena;
        private final Map<String, ArenaGame> editingGames = new HashMap<>();

        public ArenaEditorFeature(Arena arena) {
            this.arena = arena;
        }

        public EditorFeatureResponse createEditingArenaGame(String name, String type) {
            GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
            if (gameFeature.isGameExist(name) || editingGames.containsKey(name)) {
                return EditorFeatureResponse.GAME_EXISTED;
            }
            Optional<ArenaGame> optionalGame = ArenaGameBuilder.INSTANCE.build(type, Pair.of(arena, name));
            if (optionalGame.isPresent()) {
                ArenaGame game = optionalGame.get();
                editingGames.put(name, game);
                game.setSetting("type", type);
                return EditorFeatureResponse.SUCCESS;
            } else {
                return EditorFeatureResponse.TYPE_NOT_FOUND;
            }
        }

        public Optional<ArenaGame> getGame(String name) {
            ArenaGame game = editingGames.get(name);
            if (game == null) {
                game = arena.getArenaFeature(GameFeature.class).getGame(name).orElse(null);
            }
            return Optional.ofNullable(game);
        }

        public List<String> getEditingGames() {
            return List.copyOf(editingGames.keySet());
        }
    }
}
