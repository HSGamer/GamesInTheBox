package me.hsgamer.gamesinthebox.util;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.api.BlockUtil;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.CollectionUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class Utils {
    private Utils() {
        // EMPTY
    }

    public static Optional<TimeUnit> parseTimeUnit(String timeUnit) {
        try {
            return Optional.of(TimeUnit.valueOf(timeUnit.toUpperCase(Locale.ROOT)));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static ConfigurationSection createSection(Map<String, Object> values) {
        MemoryConfiguration section = new MemoryConfiguration();
        section.addDefaults(values);
        return section;
    }

    public static Map<XMaterial, Integer> parseMaterialNumberMap(Map<String, Object> values) {
        Map<XMaterial, Integer> map = new EnumMap<>(XMaterial.class);
        values.forEach((k, v) -> {
            Optional<XMaterial> optionalXMaterial = XMaterial.matchXMaterial(k);
            if (optionalXMaterial.isEmpty()) return;
            int value;
            try {
                value = Integer.parseInt(Objects.toString(v));
            } catch (Exception e) {
                return;
            }
            map.put(optionalXMaterial.get(), value);
        });
        return map;
    }

    public static ProbabilityCollection<XMaterial> parseMaterialProbability(Map<String, Object> values) {
        ProbabilityCollection<XMaterial> collection = new ProbabilityCollection<>();
        parseMaterialNumberMap(values).forEach(collection::add);
        return collection;
    }

    public static void cancelSafe(BukkitTask task) {
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // IGNORED
            }
        }
    }

    public static void despawnSafe(Entity entity) {
        if (entity != null && entity.isValid()) {
            try {
                entity.remove();
            } catch (Exception ignored) {
                // IGNORED
            }
        }
    }

    public static String getRandomColorizedString(Collection<String> collection, String defaultValue) {
        String s = Optional.ofNullable(CollectionUtils.pickRandom(collection)).orElse(defaultValue);
        return MessageUtils.colorize(s);
    }

    public static EntityType tryGetLivingEntityType(String entityType, EntityType defaultValue) {
        try {
            EntityType type = EntityType.valueOf(entityType.toUpperCase(Locale.ROOT));
            Class<? extends Entity> clazz = type.getEntityClass();
            if (clazz != null && LivingEntity.class.isAssignableFrom(clazz)) {
                return type;
            }
        } catch (Exception ignored) {
            // IGNORED
        }
        return defaultValue;
    }

    public static void clearAllBlocks(Collection<Location> locations) {
        HashSet<Chunk> chunks = new HashSet<>();
        locations.forEach(location -> {
            Block block = location.getBlock();
            BlockUtil.getHandler().setBlock(block, Material.AIR, (byte) 0, true, true);
            BlockUtil.updateLight(block);
            chunks.add(block.getChunk());
        });
        chunks.forEach(chunk -> BlockUtil.getHandler().sendChunkUpdate(chunk));
    }
}
