package me.hsgamer.gamesinthebox.hologram.dh;

import eu.decentsoftware.holograms.api.DHAPI;
import me.hsgamer.gamesinthebox.api.Hologram;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class DHHologram implements Hologram {
    private final String name;
    private final Location location;
    private eu.decentsoftware.holograms.api.holograms.Hologram hologram;

    public DHHologram(String name, Location location) {
        this.name = name + "-" + UUID.randomUUID();
        this.location = location;
    }

    @Override
    public void init() {
        hologram = DHAPI.createHologram(name, location);
    }

    @Override
    public void setLines(List<String> lines) {
        DHAPI.setHologramLines(hologram, lines);
    }

    @Override
    public void clear() {
        hologram.delete();
    }
}
