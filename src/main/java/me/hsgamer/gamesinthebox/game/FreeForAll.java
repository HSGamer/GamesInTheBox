package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.feature.game.BlockParticleFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Map;

public class FreeForAll extends BaseArenaGame implements Listener {
    private final BoundingFeature boundingFeature;
    private final BlockParticleFeature blockParticleFeature;

    private final int pointAdd;
    private final int pointMinus;

    public FreeForAll(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this);
        blockParticleFeature = BlockParticleFeature.of(this);

        pointAdd = getInstance("point.add", 5, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getFFADisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getFFADescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of(
                "point-add", pointAdd,
                "point-minus", pointMinus
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null || killer == player) {
            return;
        }
        if (!boundingFeature.checkBounding(player.getLocation())) {
            return;
        }
        pointFeature.applyPoint(killer.getUniqueId(), pointAdd);
        pointFeature.applyPoint(player.getUniqueId(), -pointMinus);
    }

    @Override
    public void onInGameStart() {
        super.onInGameStart();
        String startMessage = instance.getMessageConfig().getFFAStartBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, startMessage));
        instance.registerListener(this);
        blockParticleFeature.start(boundingFeature);
    }

    @Override
    public void onInGameOver() {
        super.onInGameOver();
        HandlerList.unregisterAll(this);
        blockParticleFeature.stop();
    }

    @Override
    public void onEndingStart() {
        String endMessage = instance.getMessageConfig().getFFAEndBroadcast().replace("{name}", arena.getName());
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, endMessage));

        rewardFeature.tryReward(pointFeature.getTopUUID());
    }

    @Override
    public void clear() {
        HandlerList.unregisterAll(this);
        blockParticleFeature.clear();
        boundingFeature.clear();
        super.clear();
    }
}
