package me.hsgamer.gamesinthebox.feature.stratery;

import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.minigamecore.base.Arena;

import java.util.ArrayList;
import java.util.List;

public class SequencePickStrategy extends CollectionPickStrategy {
    private final List<String[]> sequence;
    private int currentIndex = 0;

    public SequencePickStrategy(Arena arena) {
        super(arena);
        this.sequence = new ArrayList<>();
    }

    @Override
    public void initCollection() {
        ConfigFeature.ArenaConfigFeature configFeature = arena.getArenaFeature(ConfigFeature.class);
        CollectionUtils.createStringListFromObject(configFeature.get("pick-sequence"), true).forEach(s -> {
            String[] split = s.split(",");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }
            sequence.add(split);
        });
    }

    @Override
    public void clearCollection() {
        sequence.clear();
    }

    @Override
    public String pickFromCollection() {
        if (sequence.isEmpty()) {
            return null;
        }
        GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
        String[] split = sequence.get(currentIndex);
        currentIndex = (currentIndex + 1) % sequence.size();
        return CollectionUtils.pickRandom(split, gameFeature::isGameExist);
    }
}
