package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.api.BlockUtil;
import me.hsgamer.blockutil.extra.iterator.BlockIteratorUtil;
import me.hsgamer.blockutil.extra.iterator.api.BlockIterator;
import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.state.InGameState;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BlockRush extends BaseArenaGame implements Listener {
    private final List<Location> blockLocations = new ArrayList<>();
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();
    private BoundingFeature boundingFeature;
    private BlockIterator blockIterator;
    private int point;
    private int blocksPerTick;
    private boolean placeOnlyOnAir;
    private ProbabilityCollection<XMaterial> materialRandomness;

    public BlockRush(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, false);
        blockIterator = BlockIteratorUtil.get(getString("bounding-iterator", "default"), boundingFeature.getBlockBox());

        point = getInstance("point", 1, Number.class).intValue();

        blocksPerTick = getInstance("blocks-per-tick", 1, Number.class).intValue();
        placeOnlyOnAir = getInstance("place-only-on-air", false, Boolean.class);

        materialRandomness = Utils.parseMaterialProbability(getValues("material", false));
        if (materialRandomness.isEmpty()) {
            materialRandomness.add(XMaterial.STONE, 1);
        }
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("boundingIterator", Editors.ofString("bounding-iterator"));
        map.put("point", Editors.ofNumber("point"));
        map.put("blocksPerTick", Editors.ofNumber("blocks-per-tick"));
        map.put("placeOnlyOnAir", Editors.ofBoolean("place-only-on-air"));
        map.put("material", Editors.ofMap("material", " "));
        map.putAll(BoundingFeature.getDefaultSettings());
        return map;
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
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getRushStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getRushEndBroadcast();
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
        super.onWaitingStart();
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
                            BlockUtil.getHandler().setBlock(block, material, xMaterial.getData(), false, true);
                            blockLocations.add(block.getLocation());
                            chunks.add(block.getChunk());
                        }
                    } else {
                        cancel();
                        isWorking.lazySet(false);
                        break;
                    }
                }
                chunks.forEach(chunk -> BlockUtil.getHandler().sendChunkUpdate(chunk));
            }
        };
        BukkitTask task = runnable.runTaskTimer(instance, 0, 0);
        currentTask.set(task);
    }

    @Override
    public boolean isWaitingOver() {
        return super.isWaitingOver() && !isWorking.get();
    }

    @Override
    public void onEndingStart() {
        super.onEndingStart();

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
                        isWorking.lazySet(false);
                        break;
                    }
                }
                chunks.forEach(chunk -> BlockUtil.getHandler().sendChunkUpdate(chunk));
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
        super.onEndingOver();
        blockIterator.reset();
        blockLocations.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void clear() {
        Utils.cancelSafe(currentTask.getAndSet(null));
        Utils.clearAllBlocks(blockLocations);
        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        blockIterator.reset();
        blockLocations.clear();
        isWorking.set(false);
        super.clear();
    }
}
