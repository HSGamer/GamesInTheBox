package me.hsgamer.gamesinthebox.arena;

import me.hsgamer.gamesinthebox.state.IdlingState;
import me.hsgamer.minigamecore.base.ArenaManager;
import me.hsgamer.minigamecore.bukkit.SimpleBukkitArena;

public class GameArena extends SimpleBukkitArena {
    public GameArena(String name, ArenaManager arenaManager) {
        super(name, arenaManager);
        setNextState(IdlingState.class);
    }
}
