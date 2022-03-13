package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.base.Feature;

import java.util.Map;
import java.util.Objects;

public class ConfigFeature extends ArenaFeature<ConfigFeature.ArenaConfigFeature> {
    private final GamesInTheBox instance;

    public ConfigFeature(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    protected ArenaConfigFeature createFeature(Arena arena) {
        return new ArenaConfigFeature(arena);
    }

    public boolean contains(Arena arena, String path) {
        return instance.getArenaConfig().contains(arena.getName() + "." + path);
    }

    public class ArenaConfigFeature implements Feature {
        private final Arena arena;

        public ArenaConfigFeature(Arena arena) {
            this.arena = arena;
        }

        public <T> T getInstance(String path, T def, Class<T> clazz) {
            return instance.getArenaConfig().getInstance(arena.getName() + "." + path, def, clazz);
        }

        public String getString(String path, String def) {
            return Objects.toString(instance.getArenaConfig().getNormalized(arena.getName() + "." + path, def));
        }

        public Object get(String path) {
            return instance.getArenaConfig().get(arena.getName() + "." + path);
        }

        public Map<String, Object> getValues(String path, boolean deep) {
            return instance.getArenaConfig().getNormalizedValues(arena.getName() + "." + path, deep);
        }

        public boolean contains(String path) {
            return instance.getArenaConfig().contains(arena.getName() + "." + path);
        }
    }
}
