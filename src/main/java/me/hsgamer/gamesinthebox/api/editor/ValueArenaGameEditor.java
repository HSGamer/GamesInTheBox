package me.hsgamer.gamesinthebox.api.editor;

import me.hsgamer.gamesinthebox.api.ArenaGame;

public abstract class ValueArenaGameEditor<T> implements ArenaGameEditor {
    private final String path;

    protected ValueArenaGameEditor(String path) {
        this.path = path;
    }

    public abstract T convert(String value);

    @Override
    public boolean edit(ArenaGame game, String value) {
        T converted;
        try {
            converted = convert(value);
        } catch (Exception e) {
            return false;
        }
        game.set(path, converted);
        return true;
    }
}
