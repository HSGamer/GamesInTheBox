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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class ShootTheBat extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;

    private final int point;
    private final int spawnOffset;
    private final int maxSpawn;

    private final List<LivingEntity> spawnedBats = new LinkedList<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public ShootTheBat(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this);
        point = getInstance("point", 1, Number.class).intValue();
        spawnOffset = getInstance("spawn-offset", 0, Number.class).intValue();
        maxSpawn = getInstance("max-spawn", 10, Number.class).intValue();
    }

    private Location getRandomLocation() {
        BlockBox box = boundingFeature.getBlockBox();
        World world = boundingFeature.getWorld();
        int minX = box.minX + spawnOffset;
        int maxX = box.maxX - spawnOffset;
        int minY = box.minY + spawnOffset;
        int maxY = box.maxY - spawnOffset;
        int minZ = box.minZ + spawnOffset;
        int maxZ = box.maxZ - spawnOffset;

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
                spawnedBats.removeIf(livingEntity -> {
                    if (!livingEntity.isValid()) {
                        return true;
                    } else if (!boundingFeature.checkBounding(livingEntity.getLocation())) {
                        livingEntity.remove();
                        return true;
                    }
                    return false;
                });
                int size = spawnedBats.size();
                if (size < maxSpawn) {
                    for (int i = 0; i < maxSpawn - size; i++) {
                        spawnedBats.add(spawnBat());
                    }
                }
            }
        }.runTaskTimer(instance, 0, 20);
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
