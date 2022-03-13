package me.hsgamer.gamesinthebox.feature.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.util.ActionBarUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PointFeature implements Feature {
    private final Map<UUID, Integer> points = new IdentityHashMap<>();
    private final BukkitTask task;
    private final AtomicBoolean updateTop = new AtomicBoolean(false);
    private final AtomicReference<List<Pair<UUID, Integer>>> topSnapshot = new AtomicReference<>(Collections.emptyList());
    private PointConsumer pointConsumer = (uuid, point, totalPoint) -> {
        // EMPTY
    };

    public PointFeature() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(getClass()), this::takeTopSnapshot, 20, 20);
    }

    public static PointFeature of(ArenaGame arenaGame) {
        PointFeature pointFeature = new PointFeature();
        pointFeature.setPointConsumer((uuid, point, totalPoint) -> {
            String message = point < 0 ? arenaGame.getInstance().getMessageConfig().getPointMinus() : arenaGame.getInstance().getMessageConfig().getPointAdd();
            int absPoint = Math.abs(point);
            message = message.replace("{name}", arenaGame.getArena().getName())
                    .replace("{point}", Integer.toString(absPoint))
                    .replace("{total}", Integer.toString(totalPoint));
            ActionBarUtils.sendActionBar(uuid, message);
        });
        return pointFeature;
    }

    private void takeTopSnapshot() {
        if (updateTop.get()) {
            List<Pair<UUID, Integer>> updatedTopSnapshot = getTop();
            topSnapshot.lazySet(updatedTopSnapshot);
        }
    }

    public void setPointConsumer(PointConsumer pointConsumer) {
        this.pointConsumer = pointConsumer;
    }

    public List<Pair<UUID, Integer>> getTopSnapshot() {
        return topSnapshot.get();
    }

    public void setTopSnapshot(boolean enable) {
        updateTop.lazySet(enable);
    }

    public void applyPoint(UUID uuid, int point) {
        if (point > 0) {
            points.merge(uuid, point, Integer::sum);
            pointConsumer.accept(uuid, point, getPoint(uuid));
        } else if (point < 0) {
            int currentPoint = getPoint(uuid);
            if (currentPoint > 0) {
                points.put(uuid, Math.max(0, currentPoint + point));
                pointConsumer.accept(uuid, Math.max(point, -currentPoint), getPoint(uuid));
            }
        } else {
            pointConsumer.accept(uuid, 0, getPoint(uuid));
        }
    }

    public int getPoint(UUID uuid) {
        return points.getOrDefault(uuid, 0);
    }

    public List<Pair<UUID, Integer>> getTop() {
        List<Pair<UUID, Integer>> list;
        if (points.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>();
            points.forEach((uuid, point) -> {
                if (point > 0) {
                    list.add(Pair.of(uuid, point));
                }
            });
            list.sort(Comparator.<Pair<UUID, Integer>>comparingInt(Pair::getValue).reversed());
        }
        return list;
    }

    public List<UUID> getTopUUID() {
        return getTop().stream().map(Pair::getKey).collect(Collectors.toList());
    }

    public void resetPointIfNotOnline() {
        points.replaceAll((uuid, point) -> Bukkit.getPlayer(uuid) == null ? 0 : point);
    }

    public void clearPoints() {
        points.clear();
    }

    @Override
    public void clear() {
        task.cancel();
    }

    public interface PointConsumer {
        void accept(UUID uuid, int point, int totalPoint);
    }
}
