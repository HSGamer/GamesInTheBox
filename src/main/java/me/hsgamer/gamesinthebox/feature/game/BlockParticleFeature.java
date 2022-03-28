package me.hsgamer.gamesinthebox.feature.game;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.util.Utils;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class BlockParticleFeature implements Feature {
    public final ParticleDisplay particleDisplay;
    public final double particleRate;
    public final long particlePeriod;
    private final AtomicReference<BukkitTask> particleTask = new AtomicReference<>();
    private BiConsumer<BlockParticleFeature, BoundingFeature> consumer = (feature, boundingFeature) -> {
        BoundingBox boundingBox = boundingFeature.getBoundingBox();
        World world = boundingFeature.getWorld();
        XParticle.structuredCube(
                boundingBox.getMin().toLocation(world),
                boundingBox.getMax().toLocation(world),
                feature.particleRate,
                feature.particleDisplay
        );
    };

    public BlockParticleFeature(ParticleDisplay particleDisplay, double particleRate, long particlePeriod) {
        this.particleDisplay = particleDisplay;
        this.particleRate = particleRate;
        this.particlePeriod = particlePeriod;
    }

    public static BlockParticleFeature of(ArenaGame arenaGame, String path) {
        ParticleDisplay particleDisplay = ParticleDisplay.fromConfig(Utils.createSection(arenaGame.getValues(path, false)));
        double particleRate = arenaGame.getInstance(path + ".rate", 0.5, Number.class).doubleValue();
        long particlePeriod = arenaGame.getInstance(path + ".period", 0L, Number.class).longValue();
        return new BlockParticleFeature(particleDisplay, particleRate, particlePeriod);
    }

    public static BlockParticleFeature of(ArenaGame arenaGame) {
        return of(arenaGame, "particle");
    }

    public void setConsumer(BiConsumer<BlockParticleFeature, BoundingFeature> consumer) {
        this.consumer = consumer;
    }

    public void start(BoundingFeature boundingFeature) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                consumer.accept(BlockParticleFeature.this, boundingFeature);
            }
        };
        BukkitTask task = runnable.runTaskTimer(JavaPlugin.getProvidingPlugin(getClass()), 0, particlePeriod);
        particleTask.set(task);
    }

    public void stop() {
        BukkitTask task = particleTask.getAndSet(null);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // IGNORED
            }
        }
    }

    @Override
    public void clear() {
        stop();
    }
}
