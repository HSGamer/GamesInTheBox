package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ShootTheBat extends BaseArenaGame implements Listener {
    private final Queue<Bat> spawnedBats = new LinkedList<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();
    private BoundingFeature boundingFeature;
    private BoundingFeature.VectorOffsetSetting vectorOffsetSetting;
    private int point;
    private int maxSpawn;

    public ShootTheBat(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, true);
        vectorOffsetSetting = BoundingFeature.VectorOffsetSetting.of(this, "spawn-offset");
        point = getInstance("point", 1, Number.class).intValue();
        maxSpawn = getInstance("max-spawn", 10, Number.class).intValue();
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("point", Editors.ofNumber("point"));
        map.put("maxSpawn", Editors.ofNumber("maxSpawn"));
        map.putAll(BoundingFeature.getDefaultSettings());
        map.putAll(BoundingFeature.VectorOffsetSetting.getSettings("spawnOffset", "spawn-offset"));
        return map;
    }

    private Bat spawnBat() {
        Location location = boundingFeature.getRandomLocation(vectorOffsetSetting);
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
                Bat currentBat = spawnedBats.poll();
                if (currentBat != null) {
                    if (currentBat.isValid() && boundingFeature.checkBounding(currentBat.getLocation())) {
                        spawnedBats.add(currentBat);
                        if (!currentBat.isAwake()) {
                            currentBat.setAwake(true);
                        }
                    } else {
                        Utils.despawnSafe(currentBat);
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
        if (!(entity instanceof Bat)) return;
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
