package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.game.BlockParticleFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.feature.game.PointFeature;
import me.hsgamer.gamesinthebox.feature.game.RewardFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KingOfTheHill extends ArenaGame {
    private final PointFeature pointFeature;
    private final BoundingFeature boundingFeature;
    private final RewardFeature rewardFeature;
    private final TimerFeature timerFeature;
    private final BlockParticleFeature blockParticleFeature;

    private final int pointAdd;
    private final int pointMinus;
    private final int maxPlayersToAddPoint;

    private final TimeUnit timeUnit;
    private final long waitingTime;
    private final long inGameTime;

    public KingOfTheHill(Arena arena, String name) {
        super(arena, name);
        rewardFeature = RewardFeature.of(this);
        boundingFeature = BoundingFeature.of(this);
        pointFeature = PointFeature.of(this);
        blockParticleFeature = BlockParticleFeature.of(this);

        pointAdd = getInstance("point.add", 5, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();
        maxPlayersToAddPoint = getInstance("point.max-players-to-add", -1, Number.class).intValue();

        timerFeature = arena.getArenaFeature(CooldownFeature.class);
        timeUnit = Optional.ofNullable(getInstance("time.unit", TimeUnit.SECONDS.name(), String.class))
                .flatMap(Utils::parseTimeUnit)
                .orElse(TimeUnit.SECONDS);
        waitingTime = getInstance("time.waiting", 30L, Number.class).longValue();
        inGameTime = getInstance("time.in-game", 300L, Number.class).longValue();
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getKOTHDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getKOTHDescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of(
                "point-add", pointAdd,
                "point-minus", pointMinus,
                "max-players-to-add-point", maxPlayersToAddPoint
        );
    }

    @Override
    public List<Pair<UUID, String>> getTopList() {
        return pointFeature.getTopSnapshot()
                .stream()
                .map(point -> Pair.of(point.getKey(), Integer.toString(point.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public String getValue(UUID uuid) {
        return Integer.toString(pointFeature.getPoint(uuid));
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
        String startMessage = instance.getMessageConfig().getKOTHStartBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, startMessage));
        timerFeature.setDuration(inGameTime, timeUnit);
        pointFeature.setTopSnapshot(true);
        blockParticleFeature.start(boundingFeature);
    }

    @Override
    public boolean isInGameOver() {
        if (timerFeature.getDuration(TimeUnit.MILLISECONDS) > 0) {
            pointFeature.resetPointIfNotOnline();
            List<UUID> playersToAdd = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (!player.isDead() && boundingFeature.checkBounding(uuid)) {
                    playersToAdd.add(uuid);
                } else {
                    pointFeature.applyPoint(uuid, -pointMinus);
                }
            }
            if (maxPlayersToAddPoint < 0 || playersToAdd.size() <= maxPlayersToAddPoint) {
                playersToAdd.forEach(uuid -> pointFeature.applyPoint(uuid, pointAdd));
            } else {
                playersToAdd.forEach(uuid -> pointFeature.applyPoint(uuid, 0));
            }
            return false;
        }
        return true;
    }

    @Override
    public void onInGameOver() {
        pointFeature.setTopSnapshot(false);
        blockParticleFeature.stop();
    }

    @Override
    public void onEndingStart() {
        String endMessage = instance.getMessageConfig().getKOTHEndBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, endMessage));

        rewardFeature.tryReward(pointFeature.getTopUUID());
    }

    @Override
    public void onEndingOver() {
        pointFeature.clearPoints();
    }

    @Override
    public void clear() {
        blockParticleFeature.clear();
        pointFeature.clear();
        boundingFeature.clear();
        rewardFeature.clear();
        timerFeature.clear();
    }
}
