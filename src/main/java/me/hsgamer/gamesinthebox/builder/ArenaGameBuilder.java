package me.hsgamer.gamesinthebox.builder;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.gamesinthebox.game.*;
import me.hsgamer.hscore.builder.Builder;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Level;

public class ArenaGameBuilder extends Builder<Pair<Arena, String>, ArenaGame> {
    public static final ArenaGameBuilder INSTANCE = new ArenaGameBuilder();

    private ArenaGameBuilder() {
        registerSimple(KingOfTheHill::new, "king_of_the_hill", "koth");
        registerSimple(FreeForAll::new, "free_for_all", "ffa");
        registerSimple(BlockRush::new, "block_rush", "rush");
        registerSimple(Pinata::new, "pinata", "pin");
        registerSimple(ShootTheBat::new, "shoot_the_bat", "stb");
        registerSimple(HitTheBlock::new, "hit_the_block", "htb");
        registerSimple(DefuseTheBomb::new, "defuse_the_bomb", "dtb");
    }

    public void registerSimple(BiFunction<Arena, String, ArenaGame> function, String type, String... aliases) {
        register(pair -> function.apply(pair.getKey(), pair.getValue()), type, aliases);
    }

    public List<ArenaGame> build(Arena arena) {
        List<ArenaGame> games = new ArrayList<>();
        arena.getArenaFeature(ConfigFeature.class).getValues("settings", false).forEach((key, value) -> {
            if (!(value instanceof Map)) {
                return;
            }
            //noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) value;
            if (!map.containsKey("type")) {
                return;
            }
            String type = Objects.toString(map.get("type"));
            try {
                ArenaGameBuilder.INSTANCE.build(type, Pair.of(arena, key)).ifPresent(games::add);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, e, () -> "Failed to load game " + key + " in arena " + arena.getName());
            }
        });
        return games;
    }
}
