package me.hsgamer.gamesinthebox.feature.stratery;

import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RandomPickStrategy implements GamePickStrategy {
    private final Arena arena;
    private final ProbabilityCollection<String> probabilityCollection;
    private long waitingTime = TimeUnit.MILLISECONDS.toMillis(10);

    public RandomPickStrategy(Arena arena) {
        this.arena = arena;
        probabilityCollection = new ProbabilityCollection<>();
    }

    @Override
    public void init() {
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
        waitingTime = configFeature.getInstance("pick-waiting-time", waitingTime, Number.class).longValue();
    }

    @Override
    public void clear() {
        probabilityCollection.clear();
    }

    @Override
    public Pair<Long, String> pickGame() {
        String game;
        long tryTime = 0;
        GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
        do {
            game = probabilityCollection.get();
            if (!gameFeature.isGameExist(game)) {
                game = null;
                tryTime++;
            }
        } while (game == null && tryTime < 10);
        return Pair.of(waitingTime, game);
    }
}
