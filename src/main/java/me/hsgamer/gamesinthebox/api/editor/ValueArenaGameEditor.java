package me.hsgamer.gamesinthebox.api.editor;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public abstract class ValueArenaGameEditor<T> implements ArenaGameEditor {
    private final String path;

    protected ValueArenaGameEditor(String path) {
        this.path = path;
    }

    public abstract Optional<T> convert(CommandSender sender, String[] args);

    @Override
    public boolean edit(CommandSender sender, ArenaGame game, String[] args) {
        Optional<T> converted = convert(sender, args);
        if (converted.isPresent()) {
            game.set(path, converted.get());
            return true;
        } else {
            return false;
        }
    }
}
