package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.api.BlockUtil;
import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HitTheBlock extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;

    private final int maxBlock;
    private final ProbabilityCollection<XMaterial> materialRandomness;
    private final Map<XMaterial, Integer> materialScoreMap;
    private final int defaultPoint;

    private final List<Location> spawnBlocks = new ArrayList<>();
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public HitTheBlock(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this, false);
        maxBlock = getInstance("max-block", 10, Number.class).intValue();
        materialRandomness = Utils.parseMaterialProbability(getValues("material", false));
        materialScoreMap = Utils.parseMaterialNumberMap(getValues("material-score", false));
        defaultPoint = getInstance("default-point", 1, Number.class).intValue();

        if (materialRandomness.isEmpty()) {
            materialRandomness.add(XMaterial.STONE, 1);
        }
    }

    private Location spawnNewLocation() {
        Location location;
        do {
            location = boundingFeature.getRandomLocation();
        } while (spawnBlocks.contains(location));
        XMaterial xMaterial = materialRandomness.get();
        Material material = Optional.ofNullable(xMaterial.parseMaterial()).orElse(Material.STONE);
        Block block = location.getBlock();
        BlockUtil.getHandler().setBlock(block, material, xMaterial.getData(), false, true);
        BlockUtil.getHandler().updateLight(block);
        BlockUtil.getHandler().sendChunkUpdate(block.getChunk());
        return location;
    }

    private BukkitTask createBlockTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (spawnBlocks.size() < maxBlock) {
                    spawnBlocks.add(spawnNewLocation());
                }
            }
        }.runTaskTimer(instance, 0, 5);
    }

    private BukkitTask createClearBlockTask() {
        isWorking.set(true);
        return new BukkitRunnable() {
            private final ListIterator<Location> iterator = spawnBlocks.listIterator();
            private final HashSet<Chunk> chunks = new HashSet<>();

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    if (iterator.hasNext()) {
                        Location location = iterator.next();
                        Block block = location.getBlock();
                        chunks.add(block.getChunk());
                        BlockUtil.getHandler().setBlock(block, Material.AIR, (byte) 0, false, true);
                    } else {
                        cancel();
                        isWorking.set(false);
                        break;
                    }
                }
                chunks.forEach(chunk -> BlockUtil.getHandler().sendChunkUpdate(chunk));
                chunks.clear();
            }
        }.runTaskTimer(instance, 0, 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (spawnBlocks.contains(location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();
        if (block == null) return;
        Location location = block.getLocation();
        if (!spawnBlocks.contains(location)) return;
        XMaterial material = XMaterial.matchXMaterial(block.getType());
        BlockUtil.getHandler().setBlock(block, Material.AIR, (byte) 0, false, true);
        BlockUtil.getHandler().updateLight(block);
        BlockUtil.getHandler().sendChunkUpdate(block.getChunk());

        ProjectileSource source = event.getEntity().getShooter();
        if (!(source instanceof Player)) return;
        Player player = (Player) source;
        int score = materialScoreMap.getOrDefault(material, defaultPoint);
        pointFeature.applyPoint(player.getUniqueId(), score);
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getHitTheBlockDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getHitTheBlockDescription();
    }

    @Override
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getHitTheBlockStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getHitTheBlockEndBroadcast();
    }

    @Override
    public void onWaitingStart() {
        super.onWaitingStart();
        instance.registerListener(this);
    }

    @Override
    public void onInGameStart() {
        super.onInGameStart();
        currentTask.set(createBlockTask());
    }

    @Override
    public void onInGameOver() {
        super.onInGameOver();
        Utils.cancelSafe(currentTask.getAndSet(null));
    }

    @Override
    public void onEndingStart() {
        super.onEndingStart();
        currentTask.set(createClearBlockTask());
    }

    @Override
    public boolean isEndingOver() {
        return !isWorking.get();
    }

    @Override
    public void onEndingOver() {
        super.onEndingOver();
        Utils.cancelSafe(currentTask.getAndSet(null));
        HandlerList.unregisterAll(this);
    }

    @Override
    public void clear() {
        Utils.cancelSafe(currentTask.getAndSet(null));
        Utils.clearAllBlocks(spawnBlocks);
        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        spawnBlocks.clear();
        isWorking.set(false);
        super.clear();
    }
}
