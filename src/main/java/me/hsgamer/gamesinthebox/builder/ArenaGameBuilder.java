package me.hsgamer.gamesinthebox.builder;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.game.BlockRush;
import me.hsgamer.gamesinthebox.game.FreeForAll;
import me.hsgamer.gamesinthebox.game.KingOfTheHill;
import me.hsgamer.hscore.builder.Builder;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;

import java.util.function.BiFunction;

public class ArenaGameBuilder extends Builder<Pair<Arena, String>, ArenaGame> {
    public static final ArenaGameBuilder INSTANCE = new ArenaGameBuilder();

    private ArenaGameBuilder() {
        registerSimple(KingOfTheHill::new, "king_of_the_hill", "koth");
        registerSimple(FreeForAll::new, "free_for_all", "ffa");
        registerSimple(BlockRush::new, "block_rush", "rush");
    }

    public void registerSimple(BiFunction<Arena, String, ArenaGame> function, String type, String... aliases) {
        register(pair -> function.apply(pair.getKey(), pair.getValue()), type, aliases);
    }
}
