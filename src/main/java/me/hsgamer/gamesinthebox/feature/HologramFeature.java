package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.api.Hologram;
import me.hsgamer.gamesinthebox.api.HologramProvider;
import me.hsgamer.gamesinthebox.hologram.hd.HDHologramProvider;
import me.hsgamer.gamesinthebox.hologram.none.NoneHologramProvider;
import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HologramFeature extends ArenaFeature<HologramFeature.ArenaHologramFeature> {
    private final GamesInTheBox instance;
    private final HologramProvider hologramProvider;

    public HologramFeature(GamesInTheBox instance) {
        this.instance = instance;
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            hologramProvider = new HDHologramProvider(instance);
        } else {
            hologramProvider = new NoneHologramProvider();
        }
    }

    @Override
    public void init() {
        super.init();
        hologramProvider.init();
    }

    @Override
    public void clear() {
        super.clear();
        hologramProvider.clear();
    }

    public HologramProvider getHologramProvider() {
        return hologramProvider;
    }

    @Override
    protected ArenaHologramFeature createFeature(Arena arena) {
        return new ArenaHologramFeature(arena);
    }

    public class ArenaHologramFeature implements Feature {
        private final Arena arena;
        private final List<Pair<Hologram, List<String>>> holograms;

        public ArenaHologramFeature(Arena arena) {
            this.arena = arena;
            this.holograms = new ArrayList<>();
        }

        @Override
        public void init() {
            arena.getArenaFeature(ConfigFeature.class).getValues("hologram", false).forEach((k, v) -> {
                if (!(v instanceof Map)) return;
                //noinspection unchecked
                Map<String, Object> map = (Map<String, Object>) v;

                if (!map.containsKey("location")) return;
                List<String> rawLocations = CollectionUtils.createStringListFromObject(map.getOrDefault("location", ""), true);
                List<Location> locations = new ArrayList<>();
                rawLocations.forEach(rawLocation -> {
                    Location location = LocationUtils.getLocation(rawLocation);
                    if (location != null) {
                        locations.add(location);
                    }
                });
                if (locations.isEmpty()) return;

                List<String> texts = CollectionUtils.createStringListFromObject(map.getOrDefault("lines", instance.getMessageConfig().getDefaultHologramTemplate()), false);
                if (texts.isEmpty()) return;

                locations.forEach(location -> {
                    Hologram hologram = hologramProvider.createHologram(location);
                    hologram.init();
                    holograms.add(Pair.of(hologram, texts));
                });
            });
        }

        public void updateHolograms() {
            ArenaGame game = arena.getArenaFeature(GameFeature.class).getCurrentGame();
            List<String> description = game.getDescription();
            List<String> topDescription = game.getTopDescription();

            holograms.forEach(pair -> {
                Hologram hologram = pair.getKey();
                List<String> texts = pair.getValue();
                List<String> newTexts = new ArrayList<>();
                for (String text : texts) {
                    if (text.equalsIgnoreCase("{description}")) {
                        newTexts.addAll(description);
                    } else if (text.equalsIgnoreCase("{top-description}")) {
                        newTexts.addAll(topDescription);
                    } else {
                        newTexts.add(text);
                    }
                }
                newTexts.replaceAll(game::replace);
                newTexts.replaceAll(MessageUtils::colorize);
                hologram.setLines(newTexts);
            });
        }

        @Override
        public void clear() {
            holograms.forEach(hologramListPair -> hologramListPair.getKey().clear());
            holograms.clear();
        }
    }
}
