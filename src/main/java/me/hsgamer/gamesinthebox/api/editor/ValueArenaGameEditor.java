package me.hsgamer.gamesinthebox.api.editor;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.hscore.common.Pair;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public abstract class ValueArenaGameEditor<T> implements ArenaGameEditor {
    private final String path;

    protected ValueArenaGameEditor(String path) {
        this.path = path;
    }

    public abstract Pair<EditorResponse, Optional<T>> convert(CommandSender sender, String[] args);

    @Override
    public EditorResponse edit(CommandSender sender, ArenaGame game, boolean isCommon, String[] args) {
        Pair<EditorResponse, Optional<T>> converted = convert(sender, args);
        Optional<T> value = converted.getValue();
        value.ifPresent(t -> {
            if (isCommon) {
                game.setCommon(path, t);
            } else {
                game.setSetting(path, t);
            }
        });
        return converted.getKey();
    }
}
