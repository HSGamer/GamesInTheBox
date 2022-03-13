package me.hsgamer.gamesinthebox.state;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.GameState;

public class EndingState implements GameState {
    private final GamesInTheBox instance;

    public EndingState(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public void start(Arena arena) {
        arena.getArenaFeature(GameFeature.class).getCurrentGame().onEndingStart();
    }

    @Override
    public void update(Arena arena) {
        if (arena.getArenaFeature(GameFeature.class).getCurrentGame().isEndingOver()) {
            arena.setNextState(IdlingState.class);
        }
    }

    @Override
    public void end(Arena arena) {
        arena.getArenaFeature(GameFeature.class).getCurrentGame().onEndingOver();
    }

    @Override
    public String getDisplayName() {
        return instance.getMessageConfig().getEndingState();
    }
}
