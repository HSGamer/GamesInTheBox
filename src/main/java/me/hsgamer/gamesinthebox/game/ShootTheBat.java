package me.hsgamer.gamesinthebox.game;

import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class ShootTheBat extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;

    private final int point;
    private final int spawnOffsetMinX;
    private final int spawnOffsetMaxX;
    private final int spawnOffsetMinY;
    private final int spawnOffsetMaxY;
    private final int spawnOffsetMinZ;
    private final int spawnOffsetMaxZ;
    private final int maxSpawn;

    private final Queue<LivingEntity> spawnedBats = new LinkedList<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public ShootTheBat(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this, true);
        point = getInstance("point", 1, Number.class).intValue();
        spawnOffsetMinX = getInstance("spawn-offset.min-x", 0, Number.class).intValue();
        spawnOffsetMaxX = getInstance("spawn-offset.max-x", 0, Number.class).intValue();
        spawnOffsetMinY = getInstance("spawn-offset.min-y", 0, Number.class).intValue();
        spawnOffsetMaxY = getInstance("spawn-offset.max-y", 0, Number.class).intValue();
        spawnOffsetMinZ = getInstance("spawn-offset.min-z", 0, Number.class).intValue();
        spawnOffsetMaxZ = getInstance("spawn-offset.max-z", 0, Number.class).intValue();
        maxSpawn = getInstance("max-spawn", 10, Number.class).intValue();
    }

    private Location getRandomLocation() {
        BlockBox box = boundingFeature.getBlockBox();
        World world = boundingFeature.getWorld();
        int minX = box.minX + spawnOffsetMinX;
        int maxX = box.maxX - spawnOffsetMaxX;
        int minY = box.minY + spawnOffsetMinY;
        int maxY = box.maxY - spawnOffsetMaxY;
        int minZ = box.minZ + spawnOffsetMinZ;
        int maxZ = box.maxZ - spawnOffsetMaxZ;

        int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int y = ThreadLocalRandom.current().nextInt(minY, maxY + 1);
        int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

        return new Location(world, x, y, z);
    }

    private LivingEntity spawnBat() {
        Location location = getRandomLocation();
        World world = location.getWorld();
        assert world != null;
        return world.spawn(location, Bat.class, bat -> {
            bat.setHealth(2);
            bat.setRemoveWhenFarAway(false);
            bat.setGlowing(true);
            bat.setCustomName(getRandomNameTag());
            bat.setCustomNameVisible(true);
        });
    }

    private BukkitTask createBatTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity currentBat = spawnedBats.poll();
                if (currentBat != null) {
                    if (currentBat.isValid() && boundingFeature.checkBounding(currentBat.getLocation())) {
                        spawnedBats.add(currentBat);
                    } else if (currentBat.isValid()) {
                        currentBat.remove();
                    }
                }
                int size = spawnedBats.size();
                if (size < maxSpawn) {
                    spawnedBats.add(spawnBat());
                }
            }
        }.runTaskTimer(instance, 0, 5);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBatDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!spawnedBats.remove(entity)) return;

        Player player = entity.getKiller();
        if (player == null) return;
        pointFeature.applyPoint(player.getUniqueId(), point);
    }

    private String getRandomNameTag() {
        return Utils.getRandomColorizedString(instance.getMessageConfig().getShootTheBatNameTag(), "&c&lSHOOT ME");
    }

    @Override
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getShootTheBatStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getShootTheBatEndBroadcast();
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getShootTheBatDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getShootTheBatDescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of("point", point);
    }

    @Override
    public void onInGameStart() {
        super.onInGameStart();
        currentTask.set(createBatTask());
        instance.registerListener(this);
    }

    @Override
    public void onInGameOver() {
        super.onInGameOver();
        HandlerList.unregisterAll(this);
        Utils.cancelSafe(currentTask.getAndSet(null));
        spawnedBats.forEach(Utils::despawnSafe);
        spawnedBats.clear();
    }

    @Override
    public void clear() {
        HandlerList.unregisterAll(this);
        Utils.cancelSafe(currentTask.getAndSet(null));
        spawnedBats.forEach(Utils::despawnSafe);
        spawnedBats.clear();
        boundingFeature.clear();
        super.clear();
    }
}
