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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
    private final EntityType entityType;
    private final double pinataSpeed;
    private final int maxNoDamageTicks;
    private final boolean babyEntity;
    private final boolean glowing;

    private final AtomicReference<LivingEntity> currentPinata = new AtomicReference<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();

    public Pinata(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this, true);
        spawnLocation = Objects.requireNonNull(LocationUtils.getLocation(getString("spawn-location", "")), "spawn-location is null");
        particleDisplay = ParticleDisplay.fromConfig(Utils.createSection(getValues("particle", false)));
        pinataSpeed = getInstance("pinata.speed", -1, Number.class).doubleValue();
        maxNoDamageTicks = getInstance("pinata.max-no-damage-ticks", -1, Number.class).intValue();
        entityType = Utils.tryGetLivingEntityType(getString("pinata.type", "SHEEP"), EntityType.SHEEP);
        babyEntity = getInstance("pinata.baby", false, Boolean.class);
        glowing = getInstance("pinata.glowing", false, Boolean.class);
    }

    private LivingEntity spawnPinata() {
        World world = spawnLocation.getWorld();
        assert world != null;
        LivingEntity pinata = (LivingEntity) world.spawnEntity(spawnLocation, entityType);
        pinata.setCustomName(getRandomNameTag());
        pinata.setCustomNameVisible(true);
        pinata.setRemoveWhenFarAway(false);
        pinata.setCanPickupItems(false);
        pinata.setGlowing(glowing);
        if (pinataSpeed >= 0) {
            AttributeInstance attributeInstance = pinata.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (attributeInstance != null) {
                attributeInstance.setBaseValue(pinataSpeed);
            }
        }
        if (maxNoDamageTicks >= 0) {
            pinata.setMaximumNoDamageTicks(maxNoDamageTicks);
        }
        if (pinata instanceof Ageable) {
            ((Ageable) pinata).setAgeLock(true);
            if (babyEntity) {
                ((Ageable) pinata).setBaby();
            } else {
                ((Ageable) pinata).setAdult();
            }
        }
        return pinata;
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
