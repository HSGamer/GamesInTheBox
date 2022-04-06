package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.BaseArenaGame;
import me.hsgamer.gamesinthebox.feature.game.BlockParticleFeature;
import me.hsgamer.gamesinthebox.feature.game.BoundingFeature;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KingOfTheHill extends BaseArenaGame {
    private final BoundingFeature boundingFeature;
    private final BlockParticleFeature blockParticleFeature;

    private final int pointAdd;
    private final int pointMinus;
    private final int maxPlayersToAddPoint;

    public KingOfTheHill(Arena arena, String name) {
        super(arena, name);
        boundingFeature = BoundingFeature.of(this);
        blockParticleFeature = BlockParticleFeature.of(this);

        pointAdd = getInstance("point.add", 5, Number.class).intValue();
        pointMinus = getInstance("point.minus", 1, Number.class).intValue();
        maxPlayersToAddPoint = getInstance("point.max-players-to-add", -1, Number.class).intValue();
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
