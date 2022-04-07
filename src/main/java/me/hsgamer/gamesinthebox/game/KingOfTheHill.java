package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.Editors;
import me.hsgamer.gamesinthebox.feature.game.BlockParticleFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class KingOfTheHill extends BaseArenaGame {
    private BoundingFeature boundingFeature;
    private BlockParticleFeature blockParticleFeature;
    private int pointAdd;
    private int pointMinus;
    private int maxPlayersToAddPoint;

    public KingOfTheHill(Arena arena, String name) {
        super(arena, name);
    }

    @Override
    public void init() {
        super.init();
        boundingFeature = BoundingFeature.of(this, true);
        blockParticleFeature = BlockParticleFeature.of(this);

        pointAdd = getInstance("point.add", 5, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();
        maxPlayersToAddPoint = getInstance("point.max-players-to-add", -1, Number.class).intValue();
    }

    @Override
    protected Map<String, ArenaGameEditor> getAdditionalEditors() {
        Map<String, ArenaGameEditor> map = new HashMap<>();
        map.put("pointAdd", Editors.ofNumber("point.add"));
        map.put("pointMinus", Editors.ofNumber("point.minus"));
        map.put("maxPlayersToAddPoint", Editors.ofNumber("point.max-players-to-add"));
        map.putAll(BoundingFeature.getDefaultSettings());
        return map;
    }

    @Override
    public String getDefaultDisplayName() {
        return instance.getMessageConfig().getKOTHDisplayName();
    }

    @Override
    public List<String> getDefaultDescription() {
        return instance.getMessageConfig().getKOTHDescription();
    }

    @Override
    public Map<String, Object> getReplaceable() {
        return Map.of(
                "point-add", pointAdd,
                "point-minus", pointMinus,
                "max-players-to-add-point", maxPlayersToAddPoint
        );
    }

    @Override
    protected String getStartBroadcast() {
        return instance.getMessageConfig().getKOTHStartBroadcast();
    }

    @Override
    protected String getEndBroadcast() {
        return instance.getMessageConfig().getKOTHEndBroadcast();
    }

    @Override
    public void onInGameStart() {
        super.onInGameStart();
        blockParticleFeature.start(boundingFeature);
    }

    @Override
    public boolean isInGameOver() {
        if (!super.isInGameOver()) {
            pointFeature.resetPointIfNotOnline();
            List<UUID> playersToAdd = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (!player.isDead() && boundingFeature.checkBounding(uuid)) {
                    playersToAdd.add(uuid);
                } else {
                    pointFeature.applyPoint(uuid, -pointMinus);
                }
            }
            if (maxPlayersToAddPoint < 0 || playersToAdd.size() <= maxPlayersToAddPoint) {
                playersToAdd.forEach(uuid -> pointFeature.applyPoint(uuid, pointAdd));
            } else {
                playersToAdd.forEach(uuid -> pointFeature.applyPoint(uuid, 0));
            }
            return false;
        }
        return true;
    }

    @Override
    public void onInGameOver() {
        super.onInGameOver();
        blockParticleFeature.stop();
    }

    @Override
    public void clear() {
        blockParticleFeature.clear();
        boundingFeature.clear();
        super.clear();
    }
}
