package me.hsgamer.gamesinthebox.hologram.hd;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.Hologram;
import me.hsgamer.gamesinthebox.api.HologramProvider;
import me.hsgamer.gamesinthebox.util.ArenaUtils;
import me.hsgamer.minigamecore.base.ArenaManager;
import org.bukkit.Location;

import java.util.Locale;

public class HDHologramProvider implements HologramProvider {
    private final GamesInTheBox instance;

    public HDHologramProvider(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    public void init() {
        ArenaManager arenaManager = instance.getArenaManager();
        arenaManager.getAllArenas().forEach(arena -> {
            String pluginName = instance.getName().toLowerCase(Locale.ROOT);
            String name = arena.getName();
            HologramsAPI.registerPlaceholder(instance, "{" + pluginName + "_time_" + name + "}", 1, () -> arenaManager.getArenaByName(name).map(ArenaUtils::getCooldown).orElse(""));
            HologramsAPI.registerPlaceholder(instance, "{" + pluginName + "_state_" + name + "}", 1, () -> arenaManager.getArenaByName(name).map(ArenaUtils::getStateName).orElse(""));
            HologramsAPI.registerPlaceholder(instance, "{" + pluginName + "_current_game_" + name + "}", 1, () -> arenaManager.getArenaByName(name).map(ArenaUtils::getCurrentGame).orElse(""));
            for (int i = 0; i < instance.getMainConfig().getMaxTopDisplay(); i++) {
                String selector = name + ":" + i;
                int finalI = i;
                HologramsAPI.registerPlaceholder(instance, "{" + pluginName + "_top_name_" + selector + "}", 1, () -> arenaManager.getArenaByName(name).flatMap(a -> ArenaUtils.getTopName(a, finalI)).orElse(instance.getMainConfig().getNullTopName()));
                HologramsAPI.registerPlaceholder(instance, "{" + pluginName + "_top_value_" + selector + "}", 1, () -> arenaManager.getArenaByName(name).flatMap(a -> ArenaUtils.getTopValue(a, finalI)).orElse(instance.getMainConfig().getNullTopValue()));
            }
        });
    }

    @Override
    public void clear() {
        HologramsAPI.unregisterPlaceholders(instance);
    }

    @Override
    public Hologram createHologram(Location location) {
        return new HDHologram(instance, location);
    }
}
