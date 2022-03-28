package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.feature.game.PointFeature;
import me.hsgamer.gamesinthebox.feature.game.RewardFeature;
import me.hsgamer.gamesinthebox.state.InGameState;
import me.hsgamer.gamesinthebox.util.BoundingIterator;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.implementation.feature.single.TimerFeature;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BlockRush extends ArenaGame implements Listener {
    private final PointFeature pointFeature;
    private final BoundingFeature boundingFeature;
    private final RewardFeature rewardFeature;
    private final TimerFeature timerFeature;
    private final BoundingIterator boundingIterator;

    private final int point;

    private final int blocksPerTick;
    private final boolean placeOnlyOnAir;

    private final TimeUnit timeUnit;
    private final long waitingTime;
    private final long inGameTime;

    private final List<Location> blockLocations = new ArrayList<>();
    private final ProbabilityCollection<XMaterial> materialRandomness;
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public BlockRush(Arena arena, String name) {
        super(arena, name);
        rewardFeature = RewardFeature.of(this);
        boundingFeature = BoundingFeature.of(this);
        boundingIterator = BoundingIterator.Enums.get(
                        getString(
                                "bounding-iterator",
                                BoundingIterator.Enums.RANDOM_TYPE.name()
                        ))
                .get(boundingFeature.getBoundingBox(), false);

        pointFeature = PointFeature.of(this);
        point = getInstance("point", 1, Number.class).intValue();

        blocksPerTick = getInstance("blocks-per-tick", 1, Number.class).intValue();
        placeOnlyOnAir = getInstance("place-only-on-air", false, Boolean.class);

        timerFeature = arena.getArenaFeature(CooldownFeature.class);
        timeUnit = Optional.ofNullable(getInstance("time.unit", TimeUnit.SECONDS.name(), String.class))
                .flatMap(Utils::parseTimeUnit)
                .orElse(TimeUnit.SECONDS);
        waitingTime = getInstance("time.waiting", 30L, Number.class).longValue();
        inGameTime = getInstance("time.in-game", 300L, Number.class).longValue();

        materialRandomness = Utils.parseMaterialProbability(getValues("material", false));
        if (materialRandomness.isEmpty()) {
            materialRandomness.add(XMaterial.STONE, 1);
        }
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getRushDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getRushDescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of("point", point);
    }

    @Override
    public List<Pair<UUID, String>> getTopList() {
        return pointFeature.getTopSnapshot()
                .stream()
                .map(pair -> Pair.of(pair.getKey(), Integer.toString(pair.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public String getValue(UUID uuid) {
        return Integer.toString(pointFeature.getPoint(uuid));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (!blockLocations.contains(location)) return;

        if (arena.getState() == InGameState.class) {
            pointFeature.applyPoint(event.getPlayer().getUniqueId(), point);
            blockLocations.remove(location);
        } else {
            event.setCancelled(true);
        }
    }

    @Override
    public void onWaitingStart() {
        timerFeature.setDuration(waitingTime, timeUnit);
        instance.registerListener(this);
        isWorking.set(true);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (boundingIterator.hasNext()) {
                        Block block = boundingIterator.nextLocation(boundingFeature.getWorld()).getBlock();
                        if (!placeOnlyOnAir || !XTag.AIR.isTagged(XMaterial.matchXMaterial(block.getType()))) {
                            XMaterial material = materialRandomness.get();
                            XBlock.setType(block, material);
                            blockLocations.add(block.getLocation());
                        }
                    } else {
                        cancel();
                        isWorking.lazySet(false);
                        break;
                    }
                }
            }
        };
        BukkitTask task = runnable.runTaskTimer(instance, 0, 0);
        currentTask.set(task);
    }

    @Override
    public boolean isWaitingOver() {
        return timerFeature.getDuration(TimeUnit.MILLISECONDS) <= 0 && !isWorking.get();
    }

    @Override
    public void onInGameStart() {
        String startMessage = instance.getMessageConfig().getRushStartBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, startMessage));
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
    public void onEndingStart() {
        String endMessage = instance.getMessageConfig().getRushEndBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, endMessage));
        rewardFeature.tryReward(pointFeature.getTopUUID());

        Iterator<Location> iterator = blockLocations.iterator();
        isWorking.set(true);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (iterator.hasNext()) {
                        XBlock.setType(iterator.next().getBlock(), XMaterial.AIR);
                    } else {
                        cancel();
                        isWorking.lazySet(false);
                        break;
                    }
                }
            }
        };
        BukkitTask task = runnable.runTaskTimer(instance, 0, 0);
        currentTask.set(task);
    }

    @Override
    public boolean isEndingOver() {
        return !isWorking.get();
    }

    @Override
    public void onEndingOver() {
        pointFeature.clearPoints();
        boundingIterator.reset();
        blockLocations.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void clear() {
        BukkitTask task = currentTask.getAndSet(null);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // IGNORED
            }
        }
        blockLocations.forEach(location -> XBlock.setType(location.getBlock(), XMaterial.AIR));

        HandlerList.unregisterAll(this);
        pointFeature.clear();
        boundingFeature.clear();
        boundingIterator.reset();
        rewardFeature.clear();
        timerFeature.clear();
        blockLocations.clear();
        isWorking.set(false);
    }
}
