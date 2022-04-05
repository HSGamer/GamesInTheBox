package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class Pinata extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;

    private final Location spawnLocation;

    private final AtomicReference<LivingEntity> currentPinata = new AtomicReference<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public Pinata(Arena arena, String name) {
        super(arena, name);
        spawnLocation = Objects.requireNonNull(LocationUtils.getLocation(getString("spawn-location", "world,0,0,0")), "spawn-location is null");
        boundingFeature = BoundingFeature.of(this);
    }

    private LivingEntity spawnPinata() {
        World world = spawnLocation.getWorld();
        assert world != null;
        return world.spawn(spawnLocation, Sheep.class, sheep -> {
            sheep.setAdult();
            sheep.setSheared(true);
            sheep.setCustomName(MessageUtils.colorize(instance.getMessageConfig().getPinataNameTag()));
            sheep.setCustomNameVisible(true);
            Objects.requireNonNull(sheep.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(100);
            sheep.setHealth(100);
            sheep.setBreed(false);
            sheep.setMaximumNoDamageTicks(5);
            sheep.setRemoveWhenFarAway(false);
            sheep.setAI(false);
            sheep.setAgeLock(true);
            sheep.setCollidable(true);
            sheep.setCanPickupItems(false);
            sheep.setInvulnerable(false);
            sheep.setSilent(false);
            sheep.setGravity(true);
        });
    }

    private BukkitRunnable createPinataTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity pinata = currentPinata.get();
                if (pinata == null || pinata.isDead()) {
                    currentPinata.set(spawnPinata());
                } else if (!boundingFeature.checkBounding(pinata.getLocation())) {
                    pinata.teleport(spawnLocation);
                }
            }
        };
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
    public void clear() {
        Utils.cancelSafe(currentTask.getAndSet(null));
        Optional.ofNullable(currentPinata.getAndSet(null))
                .filter(livingEntity -> !livingEntity.isDead())
                .ifPresent(LivingEntity::remove);
        HandlerList.unregisterAll(this);
        boundingFeature.clear();
        super.clear();
    }
}
