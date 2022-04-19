package me.hsgamer.gamesinthebox.hologram.hd;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.Hologram;
import org.bukkit.Location;

import java.util.List;

public class HDHologram implements Hologram {
    private final GamesInTheBox instance;
    private final Location location;
    private com.gmail.filoghost.holographicdisplays.api.Hologram hologram;

    public HDHologram(GamesInTheBox instance, Location location) {
        this.instance = instance;
        this.location = location;
    }

    @Override
    public void init() {
        hologram = HologramsAPI.createHologram(instance, location);
    }

    @Override
    public void setLines(List<String> lines) {
        hologram.clearLines();
        for (String line : lines) {
            hologram.appendTextLine(line);
        }
    }

    @Override
    public void clear() {
        try {
            if (hologram != null) {
                hologram.delete();
                hologram = null;
            }
        } catch (Exception ignored) {
            // IGNORED
        }
    }
}
