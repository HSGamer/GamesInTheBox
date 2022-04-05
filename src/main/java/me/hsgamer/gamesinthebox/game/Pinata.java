package me.hsgamer.gamesinthebox.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.game.PointFeature;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

public class Pinata extends ArenaGame implements Listener {
    private final PointFeature pointFeature;

    public Pinata(Arena arena, String name) {
        super(arena, name);
        pointFeature = PointFeature.of(this);
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
    public List<Pair<UUID, String>> getTopList() {
        return pointFeature.getTopSnapshotAsStringPair();
    }

    @Override
    public String getValue(UUID uuid) {
        return Integer.toString(pointFeature.getPoint(uuid));
    }
}
