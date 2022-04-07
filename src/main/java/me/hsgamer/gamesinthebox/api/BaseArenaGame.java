package me.hsgamer.gamesinthebox.api;

import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.game.PointFeature;
import me.hsgamer.gamesinthebox.feature.game.RewardFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class BaseArenaGame extends ArenaGame {
    protected RewardFeature rewardFeature;
    protected TimerFeature timerFeature;
    protected PointFeature pointFeature;

    protected TimeUnit timeUnit;
    protected long waitingTime;
    protected long inGameTime;

    protected BaseArenaGame(Arena arena, String name) {
        super(arena, name);
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
        rewardFeature = RewardFeature.of(this);
        pointFeature = PointFeature.of(this);
        pointFeature.init();
        rewardFeature.init();
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

    protected abstract String getStartBroadcast();

    protected abstract String getEndBroadcast();

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
        String startMessage = getStartBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, startMessage));
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
    public void onEndingStart() {
        rewardFeature.tryReward(pointFeature.getTopUUID());
        String endMessage = getEndBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, endMessage));
    }

    @Override
    public void onEndingOver() {
        pointFeature.clearPoints();
    }
}
