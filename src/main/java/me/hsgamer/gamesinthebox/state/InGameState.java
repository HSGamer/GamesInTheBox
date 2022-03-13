package me.hsgamer.gamesinthebox.state;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.GameState;

public class InGameState implements GameState {
    private final GamesInTheBox instance;

    public InGameState(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public void start(Arena arena) {
        arena.getArenaFeature(GameFeature.class).getCurrentGame().onInGameStart();
    }

    @Override
    public void update(Arena arena) {
        if (arena.getArenaFeature(GameFeature.class).getCurrentGame().isInGameOver()) {
            arena.setNextState(EndingState.class);
        }
    }

    @Override
    public void end(Arena arena) {
        arena.getArenaFeature(GameFeature.class).getCurrentGame().onInGameOver();
    }

    @Override
    public String getDisplayName() {
        return instance.getMessageConfig().getInGameState();
    }
}
