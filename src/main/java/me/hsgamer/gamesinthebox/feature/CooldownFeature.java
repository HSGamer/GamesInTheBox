package me.hsgamer.gamesinthebox.feature;

import com.cronutils.model.CronType;
import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.crontime.CronTimeManager;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CooldownFeature extends ArenaFeature<CooldownFeature.ArenaCooldownFeature> {
    private final GamesInTheBox instance;

    public CooldownFeature(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public boolean isArenaSupported(Arena arena) {
        return arena.getFeature(ConfigFeature.class).contains(arena, "time");
    }

    @Override
    protected ArenaCooldownFeature createFeature(Arena arena) {
        Map<CronTimeManager, String> cronTimeManagerMap = new HashMap<>();
        arena.getArenaFeature(ConfigFeature.class).getValues("time", false).forEach((key, value) -> {
            List<String> list = CollectionUtils.createStringListFromObject(value, false);
            CronTimeManager cronTimeManager = new CronTimeManager(CronType.QUARTZ, list);
            cronTimeManagerMap.put(cronTimeManager, key);
        });
        return new ArenaCooldownFeature(arena, cronTimeManagerMap);
    }

    public class ArenaCooldownFeature extends TimerFeature {
        private final Arena arena;
        private final Map<CronTimeManager, String> map;

        public ArenaCooldownFeature(Arena arena, Map<CronTimeManager, String> map) {
            this.arena = arena;
            this.map = map;
        }

        public void start() {
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
            if (shortestKey != null) {
                gameFeature.setCurrentGame(shortestKey);
                setDuration(shortest, TimeUnit.MILLISECONDS);
            }
        }

        public String getCooldownFormat() {
            return DurationFormatUtils.formatDuration(getDuration(TimeUnit.MILLISECONDS), instance.getMainConfig().getTimeFormat());
        }
    }
}
