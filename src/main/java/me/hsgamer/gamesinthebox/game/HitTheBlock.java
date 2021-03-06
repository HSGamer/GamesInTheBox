package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
import me.hsgamer.gamesinthebox.feature.BlockFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
import java.util.concurrent.atomic.AtomicReference;

public class HitTheBlock extends BaseArenaGame implements Listener {
    private final Map<Location, Integer> spawnBlocks = new HashMap<>();
    private final AtomicReference<BlockFeature.BlockProcess> currentTask = new AtomicReference<>();
    private BoundingFeature boundingFeature;
    private int maxBlock;
    private ProbabilityCollection<XMaterial> materialRandomness;
    private Map<XMaterial, Integer> materialScoreMap;
    private int defaultPoint;
    private boolean removeProjectileOnHit;

    public HitTheBlock(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, false);
        maxBlock = getInstance("max-block", 10, Number.class).intValue();
        materialRandomness = Utils.parseMaterialProbability(getValues("material", false));
        materialScoreMap = Utils.parseMaterialNumberMap(getValues("material-score", false));
        defaultPoint = getInstance("default-point", 1, Number.class).intValue();
        removeProjectileOnHit = getInstance("remove-projectile-on-hit", false, Boolean.class);

        if (materialRandomness.isEmpty()) {
            materialRandomness.add(XMaterial.STONE, 1);
        }
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("maxBlock", Editors.ofNumber("max-block"));
        map.put("material", Editors.ofMap("material", " "));
        map.put("materialScore", Editors.ofMap("material-score", " "));
        map.put("defaultPoint", Editors.ofNumber("default-point"));
        map.put("removeProjectileOnHit", Editors.ofBoolean("remove-projectile-on-hit"));
        map.putAll(BoundingFeature.getDefaultSettings());
        return map;
    }

    private Pair<Location, XMaterial> spawnNewLocation() {
        Location location;
        do {
            location = boundingFeature.getRandomLocation();
        } while (spawnBlocks.containsKey(location));
        World world = location.getWorld();
        assert world != null;
        if (!world.getChunkAt(location).isLoaded()) return null;

        XMaterial xMaterial = materialRandomness.get();
        Block block = location.getBlock();
        XBlock.setType(block, xMaterial, false);
        return Pair.of(location, xMaterial);
    }

    private BlockFeature.BlockProcess createBlockTask() {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (spawnBlocks.size() < maxBlock) {
                    Pair<Location, XMaterial> blockPair = spawnNewLocation();
                    if (blockPair != null) {
                        Location location = blockPair.getKey();
                        int point = materialScoreMap.getOrDefault(blockPair.getValue(), defaultPoint);
                        spawnBlocks.put(location, point);
                    }
                }
            }
        }.runTaskTimer(instance, 0, 5);
        return new BlockFeature.BlockProcess() {
            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public void cancel() {
                Utils.cancelSafe(task);
            }
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (spawnBlocks.containsKey(location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();
        if (block == null) return;

        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();

        Location location = block.getLocation();
        if (!spawnBlocks.containsKey(location)) return;
        int point = spawnBlocks.remove(location);
        if (removeProjectileOnHit) projectile.remove();

        XBlock.setType(block, XMaterial.AIR, false);

        if (!(source instanceof Player)) return;
        Player player = (Player) source;
        pointFeature.applyPoint(player.getUniqueId(), point);
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
        Optional.ofNullable(currentTask.getAndSet(null)).ifPresent(BlockFeature.BlockProcess::cancel);
    }

    @Override
    public void onEndingStart() {
        super.onEndingStart();
        currentTask.set(arena.getFeature(BlockFeature.class).getBlockHandler().clearBlocks(new ArrayList<>(spawnBlocks.keySet())));
    }

    @Override
    public boolean isEndingOver() {
        return currentTask.get() == null || currentTask.get().isDone();
    }

    @Override
    public void onEndingOver() {
        super.onEndingOver();
        Optional.ofNullable(currentTask.getAndSet(null)).ifPresent(BlockFeature.BlockProcess::cancel);
        HandlerList.unregisterAll(this);
    }

    @Override
    public void clear() {
        Optional.ofNullable(currentTask.getAndSet(null)).ifPresent(BlockFeature.BlockProcess::cancel);
        arena.getFeature(BlockFeature.class).getBlockHandler().clearBlocksFast(new ArrayList<>(spawnBlocks.keySet()));
        spawnBlocks.clear();
        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        super.clear();
    }
}
