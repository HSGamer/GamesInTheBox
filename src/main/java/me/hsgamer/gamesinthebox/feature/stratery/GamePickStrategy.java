package me.hsgamer.gamesinthebox.feature.stratery;

import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.Initializer;

import java.util.function.Function;

public interface GamePickStrategy extends Initializer {
    Pair<Long, String> pickGame();

    enum Enums {
        CRON(CronTimeStrategy::new),
        RANDOM(RandomPickStrategy::new),
        SEQUENCE(SequencePickStrategy::new);
        private final Function<Arena, GamePickStrategy> function;

        Enums(Function<Arena, GamePickStrategy> function) {
            this.function = function;
        }

        public static Enums get(String name) {
            for (Enums e : Enums.values()) {
                if (e.name().equalsIgnoreCase(name)) {
                    return e;
                }
            }
            return CRON;
        }

        public GamePickStrategy get(Arena arena) {
            return function.apply(arena);
        }
    }
}
