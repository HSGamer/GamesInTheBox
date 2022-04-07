package me.hsgamer.gamesinthebox.api.editor;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import org.bukkit.command.CommandSender;

public interface ArenaGameEditor {
    boolean edit(CommandSender sender, ArenaGame game, String[] args);
}
