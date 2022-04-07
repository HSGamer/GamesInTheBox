package me.hsgamer.gamesinthebox.feature.stratery;

import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.minigamecore.base.Arena;

import java.util.Objects;

public class RandomPickStrategy extends CollectionPickStrategy {
    private final ProbabilityCollection<String> probabilityCollection;

    public RandomPickStrategy(Arena arena) {
        super(arena);
        probabilityCollection = new ProbabilityCollection<>();
    }

    @Override
    public void initCollection() {
        ConfigFeature.ArenaConfigFeature configFeature = arena.getArenaFeature(ConfigFeature.class);
        configFeature.getValues("pick-chance", false).forEach((key, value) -> {
            int chance;
            try {
                chance = Integer.parseInt(Objects.toString(value));
            } catch (Exception e) {
                chance = 0;
            }
            probabilityCollection.add(key, chance);
        });
    }

    @Override
    public void clearCollection() {
        probabilityCollection.clear();
    }

    @Override
    public String pickFromCollection() {
        String game;
        GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
        if (!probabilityCollection.isEmpty()) {
            long tryTime = 0;
            do {
                game = probabilityCollection.get();
                if (!gameFeature.isGameExist(game)) {
                    game = null;
                    tryTime++;
                }
            } while (game == null && tryTime < 10);
        } else {
            game = CollectionUtils.pickRandom(gameFeature.getAvailableGames());
        }
        return game;
    }
}
