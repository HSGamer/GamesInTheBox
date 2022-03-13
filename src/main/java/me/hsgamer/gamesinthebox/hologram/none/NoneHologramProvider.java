package me.hsgamer.gamesinthebox.hologram.none;

import me.hsgamer.gamesinthebox.api.Hologram;
import me.hsgamer.gamesinthebox.api.HologramProvider;
import org.bukkit.Location;

public class NoneHologramProvider implements HologramProvider {
    private static final Hologram hologram = lines -> {
        // Do nothing
    };

    @Override
    public Hologram createHologram(Location location) {
        return hologram;
    }
}
