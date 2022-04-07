package me.hsgamer.gamesinthebox.api.editor;

import java.util.function.Function;

public class SimpleValueArenaGameEditor<T> extends ValueArenaGameEditor<T> {
    private final Function<String, T> function;

    public SimpleValueArenaGameEditor(String path, Function<String, T> function) {
        super(path);
        this.function = function;
    }

    @Override
    public T convert(String value) {
        return function.apply(value);
    }
}
