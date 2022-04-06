package me.hsgamer.gamesinthebox.game;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Pinata extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;
    private final ParticleDisplay particleDisplay;

    private final Location spawnLocation;

    private final AtomicReference<LivingEntity> currentPinata = new AtomicReference<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    private final double pinataSpeed;
    private final int maxNoDamageTicks;

    public Pinata(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this, true);
        spawnLocation = Objects.requireNonNull(LocationUtils.getLocation(getString("spawn-location", "world,0,0,0")), "spawn-location is null");
        particleDisplay = ParticleDisplay.fromConfig(Utils.createSection(getValues("particle", false)));
        pinataSpeed = getInstance("pinata.speed", 0.23, Number.class).doubleValue();
        maxNoDamageTicks = getInstance("pinata.max-no-damage-ticks", 20, Number.class).intValue();
    }

    private LivingEntity spawnPinata() {
        World world = spawnLocation.getWorld();
        assert world != null;
        return world.spawn(spawnLocation, Sheep.class, sheep -> {
            sheep.setAdult();
            sheep.setCustomName(getRandomNameTag());
            sheep.setCustomNameVisible(true);
            Objects.requireNonNull(sheep.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(100);
            Objects.requireNonNull(sheep.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(pinataSpeed);
            sheep.setMaximumNoDamageTicks(maxNoDamageTicks);
            sheep.setHealth(100);
            sheep.setRemoveWhenFarAway(false);
            sheep.setAgeLock(true);
            sheep.setCanPickupItems(false);
        });
    }

    private String getRandomNameTag() {
        return Utils.getRandomColorizedString(instance.getMessageConfig().getPinataNameTag(), "&c&lPINATA");
    }

    private BukkitTask createPinataTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity pinata = currentPinata.get();
                if (pinata == null || !pinata.isValid()) {
                    currentPinata.set(spawnPinata());
                } else if (!boundingFeature.checkBounding(pinata.getLocation())) {
                    pinata.teleport(spawnLocation);
                } else {
                    particleDisplay.spawn(pinata.getLocation());
                }
            }
        }.runTaskTimer(instance, 0, 5);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (currentPinata.get() != entity) return;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
                if (e.getDamager() instanceof Player) {
                    Player player = (Player) e.getDamager();
                    int point = (int) Math.floor(e.getFinalDamage());
                    if (point > 0) {
                        pointFeature.applyPoint(player.getUniqueId(), point);
                        entity.setCustomName(getRandomNameTag());
                    }
                }
            }
            event.setDamage(0);
        }
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getPinataDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getPinataDescription();
    }

    @Override
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getPinataStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getPinataEndBroadcast();
    }

    @Override
    public void onInGameStart() {
        super.onInGameStart();
        currentPinata.set(spawnPinata());
        currentTask.set(createPinataTask());
        instance.registerListener(this);
    }

    @Override
    public void onInGameOver() {
        super.onInGameOver();
        HandlerList.unregisterAll(this);
        Utils.cancelSafe(currentTask.getAndSet(null));
        Utils.despawnSafe(currentPinata.getAndSet(null));
    }

    @Override
    public void clear() {
        Utils.cancelSafe(currentTask.getAndSet(null));
        Utils.despawnSafe(currentPinata.getAndSet(null));
        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        super.clear();
    }
}
