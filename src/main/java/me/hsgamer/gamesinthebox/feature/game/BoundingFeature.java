package me.hsgamer.gamesinthebox.feature.game;

import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BoundingFeature implements Feature {
    private final World world;
    private final BlockBox blockBox;

    public BoundingFeature(World world, BlockBox blockBox) {
        this.world = world;
        this.blockBox = blockBox;
    }

    public static BoundingFeature of(ArenaGame arenaGame, String worldPath, String pos1Path, String pos2Path, boolean maxInclusive) {
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
        return new BoundingFeature(world, new BlockBox(pos1, pos2, maxInclusive));
    }

    public static BoundingFeature of(ArenaGame arenaGame, boolean maxInclusive) {
        return of(arenaGame, "world", "pos1", "pos2", maxInclusive);
    }

    public boolean checkBounding(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && checkBounding(player.getLocation());
    }

    public boolean checkBounding(Location location) {
        if (location.getWorld() != world) {
            return false;
        }
        return blockBox.minX <= location.getX()
                && blockBox.maxX >= location.getX()
                && blockBox.minY <= location.getY()
                && blockBox.maxY >= location.getY()
                && blockBox.minZ <= location.getZ()
                && blockBox.maxZ >= location.getZ();
    }

    public World getWorld() {
        return world;
    }

    public BlockBox getBlockBox() {
        return blockBox;
    }
}
