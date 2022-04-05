package me.hsgamer.gamesinthebox.util;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    public static ProbabilityCollection<XMaterial> parseMaterialProbability(Map<String, Object> values) {
        ProbabilityCollection<XMaterial> collection = new ProbabilityCollection<>();
        values.forEach((k, v) -> {
            Optional<XMaterial> optionalXMaterial = XMaterial.matchXMaterial(k);
            if (optionalXMaterial.isEmpty()) return;
            int probability;
            try {
                probability = Integer.parseInt(Objects.toString(v));
            } catch (Exception e) {
                return;
            }
            collection.add(optionalXMaterial.get(), probability);
        });
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
}
