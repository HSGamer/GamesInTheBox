package me.hsgamer.gamesinthebox.feature.stratery;

import com.cronutils.model.CronType;
import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.hscore.crontime.CronTimeManager;
import me.hsgamer.minigamecore.base.Arena;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CronTimeStrategy implements GamePickStrategy {
    private final Arena arena;
    private final Map<CronTimeManager, String> map;

    public CronTimeStrategy(Arena arena) {
        this.arena = arena;
        this.map = new HashMap<>();
    }

    @Override
    public void init() {
        arena.getArenaFeature(ConfigFeature.class).getValues("time", false).forEach((key, value) -> {
            List<String> list = CollectionUtils.createStringListFromObject(value, false);
            CronTimeManager cronTimeManager = new CronTimeManager(CronType.QUARTZ, list);
            map.put(cronTimeManager, key);
        });
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Pair<@NotNull Long, @Nullable String> pickGame() {
        GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
        long shortest = Long.MAX_VALUE;
        String shortestKey = null;
        for (Map.Entry<CronTimeManager, String> entry : map.entrySet()) {
            long remaining = entry.getKey().getRemainingMillis();
            String name = entry.getValue();
            if (remaining < shortest && gameFeature.isGameExist(name)) {
                shortest = entry.getKey().getRemainingMillis();
                shortestKey = name;
            }
        }
        return Pair.of(shortest, shortestKey);
    }
}
