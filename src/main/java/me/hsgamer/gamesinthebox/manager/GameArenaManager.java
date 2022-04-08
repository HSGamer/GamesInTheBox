package me.hsgamer.gamesinthebox.manager;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.arena.GameArena;
import me.hsgamer.gamesinthebox.feature.*;
import me.hsgamer.gamesinthebox.state.EndingState;
import me.hsgamer.gamesinthebox.state.IdlingState;
import me.hsgamer.gamesinthebox.state.InGameState;
import me.hsgamer.gamesinthebox.state.WaitingState;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.Feature;
import me.hsgamer.minigamecore.base.GameState;
import me.hsgamer.minigamecore.implementation.manager.LoadedArenaManager;

import java.util.List;
import java.util.stream.Collectors;

public class GameArenaManager extends LoadedArenaManager {
    private final GamesInTheBox instance;

    public GameArenaManager(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    protected List<GameState> loadGameStates() {
        return List.of(
                new IdlingState(instance),
                new WaitingState(instance),
                new InGameState(instance),
                new EndingState(instance)
        );
    }

    @Override
    protected List<Feature> loadFeatures() {
        return List.of(
                new ConfigFeature(instance),
                new GameFeature(),
                new CooldownFeature(instance),
                new HologramFeature(instance),
                new TopFeature(instance),
                new EditorFeature(instance)
        );
    }

    @Override
    protected List<Arena> loadArenas() {
        return instance.getArenaConfig().getKeys(false)
                .stream()
                .map(name -> new GameArena(name, this))
                .collect(Collectors.toList());
    }

    @Override
    public void onArenaFailToLoad(Arena arena) {
        instance.getLogger().warning("Failed to load arena " + arena.getName() + ". Put to edit mode.");
        getFeature(EditorFeature.class).addArena(arena, true);
    }
}
