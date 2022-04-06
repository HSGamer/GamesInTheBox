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
import java.util.concurrent.ThreadLocalRandom;

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
        return blockBox.contains(location);
    }

    public Location getRandomLocation(VectorOffsetSetting vectorOffsetSetting) {
        int minX = blockBox.minX + vectorOffsetSetting.minXOffset;
        int maxX = blockBox.maxX - vectorOffsetSetting.maxXOffset;
        int minY = blockBox.minY + vectorOffsetSetting.minYOffset;
        int maxY = blockBox.maxY - vectorOffsetSetting.maxYOffset;
        int minZ = blockBox.minZ + vectorOffsetSetting.minZOffset;
        int maxZ = blockBox.maxZ - vectorOffsetSetting.maxZOffset;
        int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int y = ThreadLocalRandom.current().nextInt(minY, maxY + 1);
        int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
        return new Location(world, x, y, z);
    }

    public Location getRandomLocation() {
        return getRandomLocation(VectorOffsetSetting.DEFAULT);
    }

    public World getWorld() {
        return world;
    }

    public BlockBox getBlockBox() {
        return blockBox;
    }

    public static class VectorOffsetSetting {
        public static final VectorOffsetSetting DEFAULT = new VectorOffsetSetting(0, 0, 0, 0, 0, 0);
        public final int minXOffset;
        public final int maxXOffset;
        public final int minYOffset;
        public final int maxYOffset;
        public final int minZOffset;
        public final int maxZOffset;

        public VectorOffsetSetting(int minXOffset, int maxXOffset, int minYOffset, int maxYOffset, int minZOffset, int maxZOffset) {
            this.minXOffset = minXOffset;
            this.maxXOffset = maxXOffset;
            this.minYOffset = minYOffset;
            this.maxYOffset = maxYOffset;
            this.minZOffset = minZOffset;
            this.maxZOffset = maxZOffset;
        }

        public static VectorOffsetSetting of(ArenaGame arenaGame, String path) {
            return new VectorOffsetSetting(
                    arenaGame.getInstance(path + ".min-x", 0, Number.class).intValue(),
                    arenaGame.getInstance(path + ".max-x", 0, Number.class).intValue(),
                    arenaGame.getInstance(path + ".min-y", 0, Number.class).intValue(),
                    arenaGame.getInstance(path + ".max-y", 0, Number.class).intValue(),
                    arenaGame.getInstance(path + ".min-z", 0, Number.class).intValue(),
                    arenaGame.getInstance(path + ".max-z", 0, Number.class).intValue()
            );
        }
    }
}
