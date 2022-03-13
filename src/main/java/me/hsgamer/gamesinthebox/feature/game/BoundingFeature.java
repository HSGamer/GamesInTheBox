package me.hsgamer.gamesinthebox.feature.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.UUID;

public class BoundingFeature implements Feature {
    private final World world;
    private final BoundingBox boundingBox;

    public BoundingFeature(World world, BoundingBox boundingBox) {
        this.world = world;
        this.boundingBox = boundingBox;
    }

    public static BoundingFeature of(ArenaGame arenaGame, String worldPath, String pos1Path, String pos2Path) {
        String worldName = arenaGame.getString(worldPath, "");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException(arenaGame.getName() + " has invalid world");
        }
        Location pos1 = LocationUtils.getLocation(world, arenaGame.getString(pos1Path, ""));
        if (pos1 == null) {
            throw new IllegalStateException(arenaGame.getName() + " has invalid position 1");
        }
        Location pos2 = LocationUtils.getLocation(world, arenaGame.getString(pos2Path, ""));
        if (pos2 == null) {
            throw new IllegalStateException(arenaGame.getName() + " has invalid position 2");
        }
        BoundingBox boundingBox = BoundingBox.of(pos1.getBlock(), pos2.getBlock());
        return new BoundingFeature(world, boundingBox);
    }

    public static BoundingFeature of(ArenaGame arenaGame) {
        return of(arenaGame, "world", "pos1", "pos2");
    }

    public boolean checkBounding(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && checkBounding(player.getLocation());
    }

    public boolean checkBounding(Location location) {
        if (location.getWorld() != world) {
            return false;
        }
        return boundingBox.contains(location.toVector());
    }

    public World getWorld() {
        return world;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
