package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.game.PointFeature;
import me.hsgamer.gamesinthebox.feature.game.RewardFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class BaseArenaGame extends ArenaGame {
    protected final RewardFeature rewardFeature;
    protected final TimerFeature timerFeature;
    protected final PointFeature pointFeature;

    protected final TimeUnit timeUnit;
    protected final long waitingTime;
    protected final long inGameTime;

    protected BaseArenaGame(Arena arena, String name) {
        super(arena, name);
        rewardFeature = RewardFeature.of(this);
        pointFeature = PointFeature.of(this);
        timerFeature = arena.getArenaFeature(CooldownFeature.class);

        timeUnit = Optional.ofNullable(getInstance("time.unit", TimeUnit.SECONDS.name(), String.class))
                .flatMap(Utils::parseTimeUnit)
                .orElse(TimeUnit.SECONDS);
        waitingTime = getInstance("time.waiting", 30L, Number.class).longValue();
        inGameTime = getInstance("time.in-game", 300L, Number.class).longValue();
    }

    @Override
    public void clear() {
        super.clear();
        rewardFeature.clear();
        pointFeature.clear();
    }
}
