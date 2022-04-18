package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class DefuseTheBomb extends BaseArenaGame implements Listener {
    private final Queue<TNTPrimed> spawnedTNTs = new LinkedList<>();
    private final AtomicReference<BukkitTask> currentTask = new AtomicReference<>();
    private BoundingFeature boundingFeature;
    private BoundingFeature.VectorOffsetSetting vectorOffsetSetting;
    private int pointAdd;
    private int pointMinus;
    private int maxSpawn;
    private int minFuseTicks;
    private int maxFuseTicks;
    private float explodeYield;
    private boolean isDamage;

    public DefuseTheBomb(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, true);
        vectorOffsetSetting = BoundingFeature.VectorOffsetSetting.of(this, "spawn-offset");
        pointAdd = getInstance("point.add", 1, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();
        maxSpawn = getInstance("max-spawn", 10, Number.class).intValue();
        minFuseTicks = getInstance("min-fuse-ticks", 20, Number.class).intValue();
        maxFuseTicks = getInstance("max-fuse-ticks", 40, Number.class).intValue();
        explodeYield = getInstance("explode-yield", 4.0f, Number.class).floatValue();
        isDamage = getInstance("is-damage", false, Boolean.class);
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("pointAdd", Editors.ofNumber("point.add"));
        map.put("pointMinus", Editors.ofNumber("point.minus"));
        map.put("maxSpawn", Editors.ofNumber("max-spawn"));
        map.put("minFuseTicks", Editors.ofNumber("min-fuse-ticks"));
        map.put("maxFuseTicks", Editors.ofNumber("max-fuse-ticks"));
        map.put("explodeYield", Editors.ofNumber("explode-yield"));
        map.put("isDamage", Editors.ofBoolean("is-damage"));
        map.putAll(BoundingFeature.getDefaultSettings());
        map.putAll(BoundingFeature.VectorOffsetSetting.getSettings("spawnOffset", "spawn-offset"));
        return map;
    }

    private TNTPrimed spawnTNT() {
        Location location = boundingFeature.getRandomLocation(vectorOffsetSetting);
        World world = location.getWorld();
        assert world != null;
        if (!world.getChunkAt(location).isLoaded()) return null;
        return world.spawn(location, TNTPrimed.class, tnt -> {
            tnt.setFuseTicks(ThreadLocalRandom.current().nextInt(minFuseTicks, maxFuseTicks + 1));
            tnt.setYield(explodeYield);
            tnt.setIsIncendiary(false);
            tnt.setCustomNameVisible(true);
            tnt.setCustomName(getRandomNameTag());
            tnt.setGlowing(true);
        });
    }

    private BukkitTask createTNTTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                TNTPrimed currentTNT = spawnedTNTs.poll();
                if (currentTNT != null) {
                    if (currentTNT.isValid() && boundingFeature.checkBounding(currentTNT.getLocation())) {
                        spawnedTNTs.add(currentTNT);
                    } else {
                        Utils.despawnSafe(currentTNT);
                    }
                }
                int size = spawnedTNTs.size();
                if (size < maxSpawn) {
                    TNTPrimed tnt = spawnTNT();
                    if (tnt != null) {
                        spawnedTNTs.add(tnt);
                    }
                }
            }
        }.runTaskTimer(instance, 0, 1);
    }

    private String getRandomNameTag() {
        return Utils.getRandomColorizedString(instance.getMessageConfig().getDefuseTheBombNameTag(), "&c&lDEFUSE ME");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damagee = event.getEntity();
        if (!(damagee instanceof Player)) return;
        Player player = (Player) damagee;

        Entity damager = event.getDamager();
        if (!(damager instanceof TNTPrimed)) return;
        TNTPrimed tnt = (TNTPrimed) damager;
        if (!(spawnedTNTs.contains(tnt))) return;

        if (!isDamage) {
            event.setDamage(0);
        }

        pointFeature.applyPoint(player.getUniqueId(), -pointMinus);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof TNTPrimed)) return;
        TNTPrimed tnt = (TNTPrimed) entity;
        if (!(spawnedTNTs.remove(tnt))) return;

        Player player = event.getPlayer();
        pointFeature.applyPoint(player.getUniqueId(), pointAdd);
        Utils.despawnSafe(tnt);
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getDefuseTheBombDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getDefuseTheBombDescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of(
                "point-add", pointAdd,
                "point-minus", pointMinus
        );
    }

    @Override
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getDefuseTheBombStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getDefuseTheBombEndBroadcast();
    }

    @Override
    public void onInGameStart() {
        super.onInGameStart();
        currentTask.set(createTNTTask());
        instance.registerListener(this);
    }

    @Override
    public void onInGameOver() {
        super.onInGameOver();
        HandlerList.unregisterAll(this);
        Utils.cancelSafe(currentTask.getAndSet(null));
        spawnedTNTs.forEach(Utils::despawnSafe);
        spawnedTNTs.clear();
    }

    @Override
    public void clear() {
        HandlerList.unregisterAll(this);
        Utils.cancelSafe(currentTask.getAndSet(null));
        spawnedTNTs.forEach(Utils::despawnSafe);
        spawnedTNTs.clear();
        boundingFeature.clear();
        super.clear();
    }
}
