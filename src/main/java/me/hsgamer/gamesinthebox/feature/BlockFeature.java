package me.hsgamer.gamesinthebox.feature;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.blockutil.extra.iterator.api.BlockIterator;
import me.hsgamer.blockutil.extra.iterator.impl.LinearBlockIterator;
import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class BlockFeature implements Feature {
    private final GamesInTheBox instance;
    private final AtomicReference<BlockHandler> blockHandler = new AtomicReference<>();

    public BlockFeature(GamesInTheBox instance) {
        this.instance = instance;
    }

    public BlockHandler getBlockHandler() {
        BlockHandler handler = blockHandler.get();
        if (handler == null) {
            handler = new BlockHandler() {
                @Override
                public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
                    BlockIterator iterator = new LinearBlockIterator(blockBox);
                    int blocksPerTick = instance.getMainConfig().getBlocksPerTick();
                    int blockDelay = instance.getMainConfig().getBlockDelay();
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < blocksPerTick; i++) {
                                if (iterator.hasNext()) {
                                    Block block = iterator.nextLocation(world).getBlock();
                                    XBlock.setType(block, probabilityCollection.get(), false);
                                } else {
                                    cancel();
                                    future.complete(null);
                                    break;
                                }
                            }
                        }
                    }.runTaskTimer(instance, blockDelay, blockDelay);
                    return new BlockProcess() {
                        @Override
                        public boolean isDone() {
                            return future.isDone();
                        }

                        @Override
                        public void cancel() {
                            Utils.cancelSafe(task);
                        }
                    };
                }

                @Override
                public BlockProcess clearBlocks(World world, BlockBox blockBox) {
                    BlockIterator iterator = new LinearBlockIterator(blockBox);
                    int blocksPerTick = instance.getMainConfig().getBlocksPerTick();
                    int blockDelay = instance.getMainConfig().getBlockDelay();
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < blocksPerTick; i++) {
                                if (iterator.hasNext()) {
                                    Block block = iterator.nextLocation(world).getBlock();
                                    if (block.getType() != Material.AIR) {
                                        block.setType(Material.AIR, false);
                                    }
                                } else {
                                    cancel();
                                    future.complete(null);
                                    break;
                                }
                            }
                        }
                    }.runTaskTimer(instance, blockDelay, blockDelay);
                    return new BlockProcess() {
                        @Override
                        public boolean isDone() {
                            return future.isDone();
                        }

                        @Override
                        public void cancel() {
                            Utils.cancelSafe(task);
                        }
                    };
                }

                @Override
                public BlockProcess clearBlocks(Collection<Location> locations) {
                    Iterator<Location> iterator = locations.iterator();
                    int blocksPerTick = instance.getMainConfig().getBlocksPerTick();
                    int blockDelay = instance.getMainConfig().getBlockDelay();
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < blocksPerTick; i++) {
                                if (iterator.hasNext()) {
                                    Block block = iterator.next().getBlock();
                                    if (block.getType() != Material.AIR) {
                                        block.setType(Material.AIR, false);
                                    }
                                } else {
                                    cancel();
                                    future.complete(null);
                                    break;
                                }
                            }
                        }
                    }.runTaskTimer(instance, blockDelay, blockDelay);
                    return new BlockProcess() {
                        @Override
                        public boolean isDone() {
                            return future.isDone();
                        }

                        @Override
                        public void cancel() {
                            Utils.cancelSafe(task);
                        }
                    };
                }

                @Override
                public void clearBlocksFast(World world, BlockBox blockBox) {
                    for (int x = blockBox.minX; x <= blockBox.maxX; x++) {
                        for (int y = blockBox.minY; y <= blockBox.maxY; y++) {
                            for (int z = blockBox.minZ; z <= blockBox.maxZ; z++) {
                                Block block = world.getBlockAt(x, y, z);
                                if (block.getType() != Material.AIR) {
                                    block.setType(Material.AIR, false);
                                }
                            }
                        }
                    }
                }

                @Override
                public void clearBlocksFast(Collection<Location> locations) {
                    locations.forEach(location -> {
                        Block block = location.getBlock();
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR, false);
                        }
                    });
                }
            };
            blockHandler.set(handler);
        }
        return blockHandler.get();
    }

    public void setBlockHandler(BlockHandler blockHandler) {
        this.blockHandler.set(blockHandler);
    }

    public interface BlockHandler {
        BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection);

        BlockProcess clearBlocks(World world, BlockBox blockBox);

        BlockProcess clearBlocks(Collection<Location> locations);

        void clearBlocksFast(World world, BlockBox blockBox);

        void clearBlocksFast(Collection<Location> locations);
    }

    public interface BlockProcess {
        boolean isDone();

        void cancel();
    }
}
