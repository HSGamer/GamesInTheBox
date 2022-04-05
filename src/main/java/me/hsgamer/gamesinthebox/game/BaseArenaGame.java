package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.game.PointFeature;
import me.hsgamer.gamesinthebox.feature.game.RewardFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public List<Pair<UUID, String>> getTopList() {
        return pointFeature.getTopSnapshotAsStringPair();
    }

    @Override
    public String getValue(UUID uuid) {
        return Integer.toString(pointFeature.getPoint(uuid));
    }

    @Override
    public void init() {
        super.init();
        pointFeature.init();
        rewardFeature.init();
    }

    @Override
    public void clear() {
        super.clear();
        rewardFeature.clear();
        pointFeature.clear();
    }

    @Override
    public void onWaitingStart() {
        timerFeature.setDuration(waitingTime, timeUnit);
    }

    @Override
    public boolean isWaitingOver() {
        return timerFeature.getDuration(TimeUnit.MILLISECONDS) <= 0;
    }

    @Override
    public void onInGameStart() {
        timerFeature.setDuration(inGameTime, timeUnit);
        pointFeature.setTopSnapshot(true);
    }

    @Override
    public boolean isInGameOver() {
        pointFeature.resetPointIfNotOnline();
        return timerFeature.getDuration(TimeUnit.MILLISECONDS) <= 0;
    }

    @Override
    public void onInGameOver() {
        pointFeature.setTopSnapshot(false);
    }

    @Override
    public void onEndingOver() {
        pointFeature.clearPoints();
    }
}
