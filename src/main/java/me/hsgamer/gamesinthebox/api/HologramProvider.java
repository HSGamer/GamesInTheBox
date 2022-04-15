package me.hsgamer.gamesinthebox.api;

import me.hsgamer.minigamecore.base.Initializer;
import org.bukkit.Location;

public interface HologramProvider extends Initializer {
    Hologram createHologram(Location location, String name);
}
