package me.hsgamer.gamesinthebox.api.editor;

import me.hsgamer.hscore.common.Pair;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleValueArenaGameEditor<T> extends ValueArenaGameEditor<T> {
    private final BiFunction<CommandSender, String[], Pair<EditorResponse, Optional<T>>> function;

    public SimpleValueArenaGameEditor(String path, BiFunction<CommandSender, String[], Pair<EditorResponse, Optional<T>>> function) {
        super(path);
        this.function = function;
    }

    public SimpleValueArenaGameEditor(String path, Function<String[], Pair<EditorResponse, Optional<T>>> function) {
        this(path, (sender, args) -> function.apply(args));
    }

    @Override
    public Pair<EditorResponse, Optional<T>> convert(CommandSender sender, String[] args) {
        return function.apply(sender, args);
    }
}
