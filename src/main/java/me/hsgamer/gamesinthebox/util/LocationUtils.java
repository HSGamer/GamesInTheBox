package me.hsgamer.gamesinthebox.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.regex.Pattern;

public final class LocationUtils {
    private LocationUtils() {
        // EMPTY
    }

    public static Location getLocation(World world, String value) {
        String[] split = value.split(Pattern.quote(","), 3);
        if (split.length < 3) {
            return null;
        }
        try {
            return new Location(world, Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
        } catch (Exception e) {
            return null;
        }
    }


    public static Location getLocation(String value) {
        String[] split = value.split(",", 4);
        if (split.length != 4) {
            return null;
        }

        World world = Bukkit.getWorld(split[0].trim());
        if (world == null) {
            return null;
        }
        try {
            double x = Double.parseDouble(split[1].trim());
            double y = Double.parseDouble(split[2].trim());
            double z = Double.parseDouble(split[3].trim());
            return new Location(world, x, y, z);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
