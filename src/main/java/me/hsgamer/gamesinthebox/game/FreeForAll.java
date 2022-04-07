package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
import me.hsgamer.gamesinthebox.feature.game.BlockParticleFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreeForAll extends BaseArenaGame implements Listener {
    private BoundingFeature boundingFeature;
    private BlockParticleFeature blockParticleFeature;
    private int pointAdd;
    private int pointMinus;

    public FreeForAll(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, true);
        blockParticleFeature = BlockParticleFeature.of(this);

        pointAdd = getInstance("point.add", 5, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("pointAdd", Editors.ofNumber("point.add"));
        map.put("pointMinus", Editors.ofNumber("point.minus"));
        map.putAll(BoundingFeature.getDefaultSettings());
        return map;
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

    @Override
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getFFAStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getFFAEndBroadcast();
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
    public void clear() {
        HandlerList.unregisterAll(this);
        blockParticleFeature.clear();
        boundingFeature.clear();
        super.clear();
    }
}
