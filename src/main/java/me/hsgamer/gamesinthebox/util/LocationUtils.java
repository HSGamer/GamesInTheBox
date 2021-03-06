package me.hsgamer.gamesinthebox.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;
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
            double x = Double.parseDouble(split[0].trim());
            double y = Double.parseDouble(split[1].trim());
            double z = Double.parseDouble(split[2].trim());
            return new Location(world, x, y, z);
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

    public static String serializeLocation(Location location, boolean withWorld, boolean roundNumbers) {
        String world = Optional.ofNullable(location.getWorld()).map(World::getName).orElse("world");
        double x = roundNumbers ? location.getBlockX() : location.getX();
        double y = roundNumbers ? location.getBlockY() : location.getY();
        double z = roundNumbers ? location.getBlockZ() : location.getZ();
        String value = String.format("%s,%s,%s", x, y, z);
        if (withWorld) {
            value = world + "," + value;
        }
        return value;
    }
}
