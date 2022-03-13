package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.Hologram;
import me.hsgamer.gamesinthebox.api.HologramProvider;
import me.hsgamer.gamesinthebox.hologram.hd.HDHologramProvider;
import me.hsgamer.gamesinthebox.hologram.none.NoneHologramProvider;
import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Optional;

public class HologramFeature extends ArenaFeature<HologramFeature.ArenaHologramFeature> {
    private final HologramProvider hologramProvider;

    public HologramFeature(GamesInTheBox instance) {
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
        private Hologram topDescriptionHologram;
        private Hologram descriptionHologram;

        public ArenaHologramFeature(Arena arena) {
            this.arena = arena;
        }

        @Override
        public void init() {
            ConfigFeature.ArenaConfigFeature config = arena.getArenaFeature(ConfigFeature.class);
            if (config.contains("hologram.top-description")) {
                Location location = LocationUtils.getLocation(config.getString("hologram.top-description", ""));
                if (location != null) {
                    topDescriptionHologram = hologramProvider.createHologram(location);
                    topDescriptionHologram.init();
                }
            }
            if (config.contains("hologram.description")) {
                Location location = LocationUtils.getLocation(config.getString("hologram.description", ""));
                if (location != null) {
                    descriptionHologram = hologramProvider.createHologram(location);
                    descriptionHologram.init();
                }
            }
        }

        public Optional<Hologram> getTopDescriptionHologram() {
            return Optional.ofNullable(topDescriptionHologram);
        }

        public Optional<Hologram> getDescriptionHologram() {
            return Optional.ofNullable(descriptionHologram);
        }

        @Override
        public void clear() {
            if (topDescriptionHologram != null) {
                topDescriptionHologram.clear();
                topDescriptionHologram = null;
            }
            if (descriptionHologram != null) {
                descriptionHologram.clear();
                descriptionHologram = null;
            }
        }
    }
}
