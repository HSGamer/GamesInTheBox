package me.hsgamer.gamesinthebox.state;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.GameState;

public class WaitingState implements GameState {
    private final GamesInTheBox instance;

    public WaitingState(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public void start(Arena arena) {
        arena.getArenaFeature(GameFeature.class).getCurrentGame().onWaitingStart();
    }

    @Override
    public void update(Arena arena) {
        if (arena.getArenaFeature(GameFeature.class).getCurrentGame().isWaitingOver()) {
            arena.setNextState(InGameState.class);
        }
    }

    @Override
    public void end(Arena arena) {
        arena.getArenaFeature(GameFeature.class).getCurrentGame().onWaitingOver();
    }

    @Override
    public String getDisplayName() {
        return instance.getMessageConfig().getWaitingState();
    }
}
