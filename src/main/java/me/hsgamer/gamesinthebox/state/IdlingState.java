package me.hsgamer.gamesinthebox.state;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.HologramFeature;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.GameState;

import java.util.concurrent.TimeUnit;

public class IdlingState implements GameState {
    private final GamesInTheBox instance;

    public IdlingState(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public void start(Arena arena) {
        arena.getArenaFeature(CooldownFeature.class).start();
        arena.getArenaFeature(HologramFeature.class).updateHolograms();
    }

    @Override
    public void update(Arena arena) {
        if (arena.getArenaFeature(CooldownFeature.class).getDuration(TimeUnit.MILLISECONDS) <= 0) {
            arena.setNextState(WaitingState.class);
        }
    }

    @Override
    public String getDisplayName() {
        return instance.getMessageConfig().getIdlingState();
    }
}
