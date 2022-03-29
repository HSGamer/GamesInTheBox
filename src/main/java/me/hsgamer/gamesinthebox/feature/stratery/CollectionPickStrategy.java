package me.hsgamer.gamesinthebox.feature.stratery;

import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class CollectionPickStrategy implements GamePickStrategy {
    protected final Arena arena;
    private long waitingTime = TimeUnit.MILLISECONDS.toMillis(10);

    protected CollectionPickStrategy(Arena arena) {
        this.arena = arena;
    }

    public abstract void initCollection();

    public abstract void clearCollection();

    public abstract String pickFromCollection();

    @Override
    public void init() {
        ConfigFeature.ArenaConfigFeature configFeature = arena.getArenaFeature(ConfigFeature.class);
        TimeUnit timeUnit = Optional.ofNullable(configFeature.getString("pick-waiting.unit", TimeUnit.MILLISECONDS.name()))
                .flatMap(Utils::parseTimeUnit)
                .orElse(TimeUnit.MILLISECONDS);
        waitingTime = timeUnit.toMillis(configFeature.getInstance("pick-waiting.time", waitingTime, Number.class).longValue());
        initCollection();
    }

    @Override
    public void clear() {
        clearCollection();
    }

    @Override
    public Pair<Long, String> pickGame() {
        String game = pickFromCollection();
        return Pair.of(waitingTime, game);
    }
}
