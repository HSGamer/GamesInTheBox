package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FreeForAll extends ArenaGame implements Listener {
    private final PointFeature pointFeature;
    private final BoundingFeature boundingFeature;
    private final RewardFeature rewardFeature;
    private final TimerFeature timerFeature;

    private final int pointAdd;
    private final int pointMinus;

    private final TimeUnit timeUnit;
    private final long waitingTime;
    private final long inGameTime;

    public FreeForAll(Arena arena, String name) {
        super(arena, name);
        rewardFeature = RewardFeature.of(this);
        boundingFeature = BoundingFeature.of(this);
        pointFeature = PointFeature.of(this);

        pointAdd = getInstance("point.add", 5, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();

        timerFeature = arena.getArenaFeature(CooldownFeature.class);
        timeUnit = Optional.ofNullable(getInstance("time.unit", TimeUnit.SECONDS.name(), String.class))
                .flatMap(Utils::parseTimeUnit)
                .orElse(TimeUnit.SECONDS);
        waitingTime = getInstance("time.waiting", 30L, Number.class).longValue();
        inGameTime = getInstance("time.in-game", 300L, Number.class).longValue();
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getFFADisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getFFADescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of(
                "point-add", pointAdd,
                "point-minus", pointMinus
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null || killer == player) {
            return;
        }
        if (!boundingFeature.checkBounding(player.getLocation())) {
            return;
        }
        pointFeature.applyPoint(killer.getUniqueId(), pointAdd);
        pointFeature.applyPoint(player.getUniqueId(), -pointMinus);
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
        String startMessage = instance.getMessageConfig().getFFAStartBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, startMessage));
        timerFeature.setDuration(inGameTime, timeUnit);
        pointFeature.setTopSnapshot(true);
        instance.registerListener(this);
    }

    @Override
    public boolean isInGameOver() {
        pointFeature.resetPointIfNotOnline();
        return timerFeature.getDuration(TimeUnit.MILLISECONDS) <= 0;
    }

    @Override
    public void onInGameOver() {
        pointFeature.setTopSnapshot(false);
        HandlerList.unregisterAll(this);
    }

    @Override
    public void onEndingStart() {
        String endMessage = instance.getMessageConfig().getFFAEndBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, endMessage));

        rewardFeature.tryReward(pointFeature.getTopUUID());
    }

    @Override
    public void onEndingOver() {
        pointFeature.clearPoints();
    }

    @Override
    public void clear() {
        HandlerList.unregisterAll(this);
        pointFeature.clear();
        boundingFeature.clear();
        rewardFeature.clear();
        timerFeature.clear();
    }
}
