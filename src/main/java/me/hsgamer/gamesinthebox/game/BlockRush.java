package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.api.BlockUtil;
import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.blockutil.extra.iterator.BlockIteratorUtil;
import me.hsgamer.blockutil.extra.iterator.api.BlockIterator;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.state.InGameState;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BlockRush extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;
    private final BlockIterator blockIterator;

    private final int point;

    private final int blocksPerTick;
    private final boolean placeOnlyOnAir;

    private final List<Location> blockLocations = new ArrayList<>();
    private final ProbabilityCollection<XMaterial> materialRandomness;
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public BlockRush(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this);
        Vector minVector = boundingFeature.getBoundingBox().getMin();
        Vector maxVector = boundingFeature.getBoundingBox().getMax();
        BlockBox blockBox = new BlockBox(
                minVector.getBlockX(),
                minVector.getBlockY(),
                minVector.getBlockZ(),
                maxVector.getBlockX(),
                maxVector.getBlockY(),
                maxVector.getBlockZ(),
                false
        );
        blockIterator = BlockIteratorUtil.get(getString("bounding-iterator", "default"), blockBox);

        point = getInstance("point", 1, Number.class).intValue();

        blocksPerTick = getInstance("blocks-per-tick", 1, Number.class).intValue();
        placeOnlyOnAir = getInstance("place-only-on-air", false, Boolean.class);

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
        return pointFeature.getTopSnapshotAsStringPair();
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

        HashSet<Chunk> chunks = new HashSet<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (blockIterator.hasNext()) {
                        Block block = blockIterator.nextLocation(boundingFeature.getWorld()).getBlock();
                        if (!placeOnlyOnAir || !XTag.AIR.isTagged(XMaterial.matchXMaterial(block.getType()))) {
                            XMaterial xMaterial = materialRandomness.get();
                            Material material = Optional.ofNullable(xMaterial.parseMaterial()).orElse(Material.STONE);
                            BlockUtil.getHandler().setBlock(block, material, xMaterial.getData(), false, false);
                            blockLocations.add(block.getLocation());
                            chunks.add(block.getChunk());
                        }
                    } else {
                        cancel();
                        chunks.forEach(chunk -> BlockUtil.getHandler().setChunkUpdate(chunk));
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
        HashSet<Chunk> chunks = new HashSet<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (iterator.hasNext()) {
                        Block block = iterator.next().getBlock();
                        BlockUtil.getHandler().setBlock(block, Material.AIR, (byte) 0, false, false);
                        chunks.add(block.getChunk());
                    } else {
                        cancel();
                        chunks.forEach(chunk -> BlockUtil.getHandler().setChunkUpdate(chunk));
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
        blockIterator.reset();
        blockLocations.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void clear() {
        Utils.cancelSafe(currentTask.getAndSet(null));
        HashSet<Chunk> chunks = new HashSet<>();
        blockLocations.forEach(location -> {
            Block block = location.getBlock();
            BlockUtil.getHandler().setBlock(block, Material.AIR, (byte) 0, false, false);
            chunks.add(block.getChunk());
        });
        chunks.forEach(chunk -> BlockUtil.getHandler().setChunkUpdate(chunk));

        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        blockIterator.reset();
        blockLocations.clear();
        isWorking.set(false);
        super.clear();
    }
}
