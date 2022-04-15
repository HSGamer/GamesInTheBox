package me.hsgamer.gamesinthebox.hologram.dh;

import me.hsgamer.gamesinthebox.api.Hologram;
import me.hsgamer.gamesinthebox.api.HologramProvider;
import org.bukkit.Location;

public class DHHologramProvider implements HologramProvider {
    @Override
    public Hologram createHologram(Location location, String name) {
        return new DHHologram(name, location);
    }
}
