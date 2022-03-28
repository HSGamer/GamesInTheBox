package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.feature.stratery.GamePickStrategy;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.concurrent.TimeUnit;

public class CooldownFeature extends ArenaFeature<CooldownFeature.ArenaCooldownFeature> {
    private final GamesInTheBox instance;

    public CooldownFeature(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    protected ArenaCooldownFeature createFeature(Arena arena) {
        String time = arena.getArenaFeature(ConfigFeature.class).getString("pick-strategy", "");
        GamePickStrategy.Enums arenaTimeStrategyEnum = GamePickStrategy.Enums.get(time);
        GamePickStrategy arenaTimeStrategy = arenaTimeStrategyEnum.get(arena);
        return new ArenaCooldownFeature(arena, arenaTimeStrategy);
    }

    public class ArenaCooldownFeature extends TimerFeature {
        private final Arena arena;
        private final GamePickStrategy arenaTimeStrategy;

        public ArenaCooldownFeature(Arena arena, GamePickStrategy gamePickStrategy) {
            this.arena = arena;
            this.arenaTimeStrategy = gamePickStrategy;
        }

        @Override
        public void init() {
            arenaTimeStrategy.init();
        }

        @Override
        public void clear() {
            arenaTimeStrategy.clear();
        }

        public void start() {
            Pair<Long, String> pair = arenaTimeStrategy.pickGame();
            GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
            long time = pair.getKey();
            String arenaGame = pair.getValue();
            if (arenaGame != null && gameFeature.isGameExist(arenaGame)) {
                gameFeature.setCurrentGame(arenaGame);
                setDuration(time, TimeUnit.MILLISECONDS);
            } else {
                setDuration(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
        }

        public String getCooldownFormat() {
            return DurationFormatUtils.formatDuration(getDuration(TimeUnit.MILLISECONDS), instance.getMainConfig().getTimeFormat());
        }
    }
}
