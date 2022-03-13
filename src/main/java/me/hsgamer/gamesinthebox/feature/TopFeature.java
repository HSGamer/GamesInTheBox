package me.hsgamer.gamesinthebox.feature;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.state.EndingState;
import me.hsgamer.gamesinthebox.state.InGameState;
import me.hsgamer.gamesinthebox.state.WaitingState;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.ArenaFeature;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TopFeature extends ArenaFeature<TopFeature.ArenaTopFeature> {
    private final GamesInTheBox instance;

    public TopFeature(GamesInTheBox instance) {
        this.instance = instance;
    }

    @Override
    protected ArenaTopFeature createFeature(Arena arena) {
        return new ArenaTopFeature(arena);
    }

    public class ArenaTopFeature extends BukkitRunnable implements Feature {
        private final Arena arena;
        private final AtomicReference<List<Pair<UUID, String>>> top = new AtomicReference<>(Collections.emptyList());
        private final AtomicReference<List<UUID>> topUUIDs = new AtomicReference<>(Collections.emptyList());

        public ArenaTopFeature(Arena arena) {
            this.arena = arena;
        }

        @Override
        public void init() {
            runTaskTimerAsynchronously(instance, 0, 20);
        }

        @Override
        public void run() {
            if (arena.getState() == InGameState.class || arena.getState() == EndingState.class) {
                ArenaGame arenaGame = arena.getArenaFeature(GameFeature.class).getCurrentGame();
                setTop(arenaGame.getTopList());
            } else if (arena.getState() == WaitingState.class) {
                setTop(Collections.emptyList());
            }
        }

        @Override
        public void clear() {
            cancel();
        }

        public List<Pair<UUID, String>> getTop() {
            return top.get();
        }

        private void setTop(List<Pair<UUID, String>> top) {
            this.top.lazySet(top);
            this.topUUIDs.lazySet(top.stream().map(Pair::getKey).collect(Collectors.toList()));
        }

        public int getTopIndex(UUID uuid) {
            return topUUIDs.get().indexOf(uuid);
        }

        public Optional<Pair<UUID, String>> getTop(int index) {
            List<Pair<UUID, String>> getTopSnapshot = getTop();
            if (getTopSnapshot.size() <= index) {
                return Optional.empty();
            } else {
                return Optional.of(getTopSnapshot.get(index));
            }
        }
    }
}
