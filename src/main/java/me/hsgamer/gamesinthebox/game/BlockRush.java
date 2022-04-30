package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
import me.hsgamer.gamesinthebox.feature.BlockFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.state.InGameState;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class BlockRush extends BaseArenaGame implements Listener {
    private final AtomicReference<BlockFeature.BlockProcess> currentTask = new AtomicReference<>();
    private BoundingFeature boundingFeature;
    private int point;
    private ProbabilityCollection<XMaterial> materialRandomness;

    public BlockRush(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, false);

        point = getInstance("point", 1, Number.class).intValue();

        materialRandomness = Utils.parseMaterialProbability(getValues("material", false));
        if (materialRandomness.isEmpty()) {
            materialRandomness.add(XMaterial.STONE, 1);
        }
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("point", Editors.ofNumber("point"));
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
        if (!boundingFeature.checkBounding(location)) return;

        if (arena.getState() == InGameState.class) {
            pointFeature.applyPoint(event.getPlayer().getUniqueId(), point);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlocExplode(BlockExplodeEvent event) {
        if (arena.getState() != InGameState.class) {
            event.blockList().removeIf(block -> boundingFeature.checkBounding(block.getLocation()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (arena.getState() != InGameState.class) {
            event.blockList().removeIf(block -> boundingFeature.checkBounding(block.getLocation()));
            return;
        }

        Entity entity = event.getEntity();
        if (entity.getWorld() != boundingFeature.getWorld()) return;
        Entity source;
        do {
            source = entity instanceof TNTPrimed ? ((TNTPrimed) entity).getSource() : null;
            entity = source;
        } while (source instanceof TNTPrimed);

        int count = 0;
        for (Block block : event.blockList()) {
            if (XTag.AIR.isTagged(XMaterial.matchXMaterial(block.getType()))) continue;
            if (boundingFeature.checkBounding(block.getLocation())) {
                count++;
            }
        }

        if (count > 0 && source instanceof Player) {
            pointFeature.applyPoint(source.getUniqueId(), point * count);
        }
    }

    @Override
    public void onWaitingStart() {
        super.onWaitingStart();
        instance.registerListener(this);
        BlockFeature.BlockProcess process = arena.getFeature(BlockFeature.class).getBlockHandler().setRandomBlocks(
                boundingFeature.getWorld(), boundingFeature.getBlockBox(),
                materialRandomness
        );
        currentTask.set(process);
    }

    @Override
    public boolean isWaitingOver() {
        return super.isWaitingOver() && (currentTask.get() == null || currentTask.get().isDone());
    }

    @Override
    public void onEndingStart() {
        super.onEndingStart();
        BlockFeature.BlockProcess process = arena.getFeature(BlockFeature.class).getBlockHandler().clearBlocks(
                boundingFeature.getWorld(), boundingFeature.getBlockBox()
        );
        currentTask.set(process);
    }

    @Override
    public boolean isEndingOver() {
        return super.isEndingOver() && (currentTask.get() == null || currentTask.get().isDone());
    }

    @Override
    public void onEndingOver() {
        super.onEndingOver();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void clear() {
        Optional.ofNullable(currentTask.get()).ifPresent(BlockFeature.BlockProcess::cancel);
        arena.getFeature(BlockFeature.class).getBlockHandler().clearBlocksFast(boundingFeature.getWorld(), boundingFeature.getBlockBox());
        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        super.clear();
    }
}
