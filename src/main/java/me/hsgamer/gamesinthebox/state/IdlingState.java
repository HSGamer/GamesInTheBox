package me.hsgamer.gamesinthebox.state;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.gamesinthebox.feature.HologramFeature;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IdlingState implements GameState {
    private final GamesInTheBox instance;

    public IdlingState(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public void start(Arena arena) {
        arena.getArenaFeature(CooldownFeature.class).start();

        ArenaGame arenaGame = arena.getArenaFeature(GameFeature.class).getCurrentGame();
        if (arenaGame != null) {
            arena.getArenaFeature(HologramFeature.class).getDescriptionHologram().ifPresent(hologram -> {
                List<String> generatedDescription = new ArrayList<>();
                for (String line : instance.getMessageConfig().getHologramDescription()) {
                    if (line.equals("{description}")) {
                        generatedDescription.addAll(arenaGame.getDescription());
                    } else {
                        generatedDescription.add(line);
                    }
                }
                generatedDescription.replaceAll(MessageUtils::colorize);
                hologram.setLines(generatedDescription);
            });
            arena.getArenaFeature(HologramFeature.class).getTopDescriptionHologram().ifPresent(hologram -> {
                List<String> generatedDescription = new ArrayList<>();
                for (String line : instance.getMessageConfig().getHologramTopDescription()) {
                    if (line.equals("{description}")) {
                        generatedDescription.addAll(arenaGame.getTopDescription());
                    } else {
                        generatedDescription.add(line);
                    }
                }
                generatedDescription.replaceAll(MessageUtils::colorize);
                hologram.setLines(generatedDescription);
            });
        }
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
