package me.hsgamer.gamesinthebox.api.editor;

import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.function.BiFunction;

public class SimpleValueArenaGameEditor<T> extends ValueArenaGameEditor<T> {
    private final BiFunction<CommandSender, String[], Optional<T>> function;

    public SimpleValueArenaGameEditor(String path, BiFunction<CommandSender, String[], Optional<T>> function) {
        super(path);
        this.function = function;
    }

    @Override
    public Optional<T> convert(CommandSender sender, String[] args) {
        return function.apply(sender, args);
    }
}
