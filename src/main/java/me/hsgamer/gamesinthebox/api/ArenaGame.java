package me.hsgamer.gamesinthebox.api;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.editor.ArenaGameEditor;
import me.hsgamer.gamesinthebox.api.editor.EditorResponse;
import me.hsgamer.gamesinthebox.feature.ConfigFeature;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.minigamecore.base.Arena;
import me.hsgamer.minigamecore.base.Initializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class ArenaGame implements Initializer {
    protected final Arena arena;
    protected final ConfigFeature.ArenaConfigFeature configFeature;
    protected final GamesInTheBox instance;
    protected final String name;
    protected final Map<String, ArenaGameEditor> editors;

    protected ArenaGame(Arena arena, String name) {
        this.arena = arena;
        this.configFeature = arena.getArenaFeature(ConfigFeature.class);
        this.name = name;
        this.instance = JavaPlugin.getPlugin(GamesInTheBox.class);
        this.editors = getAvailableEditors();
    }

    public final String getName() {
        return name;
    }

    private String getSettingPath(String path) {
        return "settings." + name + "." + path;
    }

    public String getCommonPath(String path) {
        return "common." + path;
    }

    public final String getString(String path, String def) {
        return configFeature.getString(getSettingPath(path), configFeature.getString(getCommonPath(path), def));
    }

    public final <T> T getInstance(String path, T def, Class<T> clazz) {
        return configFeature.getInstance(getSettingPath(path), configFeature.getInstance(getCommonPath(path), def, clazz), clazz);
    }

    public final Object get(String path) {
        Object value = configFeature.get(getSettingPath(path));
        if (value == null) {
            value = configFeature.get(getCommonPath(path));
        }
        return value;
    }

    public final Map<String, Object> getValues(String path, boolean deep) {
        if (containsSetting(path)) {
            return configFeature.getValues(getSettingPath(path), deep);
        } else if (containsCommon(path)) {
            return configFeature.getValues(getCommonPath(path), deep);
        } else {
            return Collections.emptyMap();
        }
    }

    public final boolean containsSetting(String path) {
        return configFeature.contains(getSettingPath(path));
    }

    public final boolean containsCommon(String path) {
        return configFeature.contains(getCommonPath(path));
    }

    public final boolean containsPath(String path) {
        return containsSetting(path) || containsCommon(path);
    }

    public final void setSetting(String path, Object value) {
        configFeature.set(getSettingPath(path), value);
    }

    public final void setCommon(String path, Object value) {
        configFeature.set(getCommonPath(path), value);
    }

    protected Map<String, ArenaGameEditor> getAvailableEditors() {
        return Collections.emptyMap();
    }

    public Map<String, ArenaGameEditor> getEditors() {
        return Collections.unmodifiableMap(editors);
    }

    public EditorResponse edit(CommandSender sender, String editor, boolean isCommon, String... args) {
        ArenaGameEditor arenaGameEditor = editors.get(editor);
        if (arenaGameEditor == null) {
            return EditorResponse.NOT_FOUND;
        }
        return arenaGameEditor.edit(sender, this, isCommon, args);
    }

    public void onWaitingStart() {
        // EMPTY
    }

    public boolean isWaitingOver() {
        return true;
    }

    public void onWaitingOver() {
        // EMPTY
    }

    public void onInGameStart() {
        // EMPTY
    }

    public boolean isInGameOver() {
        return true;
    }

    public void onInGameOver() {
        // EMPTY
    }

    public void onEndingStart() {
        // EMPTY
    }

    public boolean isEndingOver() {
        return true;
    }

    public void onEndingOver() {
        // EMPTY
    }

    public String getDisplayName() {
        String displayName;
        if (containsSetting("display-name")) {
            displayName = getString("display-name", getDefaultDisplayName());
        } else {
            displayName = getDefaultDisplayName();
        }
        return displayName;
    }

    public List<String> getDescription() {
        List<String> description;
        if (containsSetting("description")) {
            description = CollectionUtils.createStringListFromObject(get("description"), false);
        } else {
            description = getDefaultDescription();
        }
        return description;
    }

    public Map<String, List<String>> getAdditionalDescription() {
        Map<String, List<String>> description = new HashMap<>();
        if (containsSetting("additional-description")) {
            getValues("additional-description", false).forEach((key, value) -> {
                List<String> list = CollectionUtils.createStringListFromObject(value, false);
                description.put(key, list);
            });
        }
        return description;
    }

    public final String replace(String str) {
        Map<String, Object> replace = getReplaceable();
        for (Map.Entry<String, Object> entry : replace.entrySet()) {
            str = str.replace("{" + entry.getKey() + "}", Objects.toString(entry.getValue()));
        }
        str = str.replace("{name}", name)
                .replace("{display-name}", getDisplayName());
        return str;
    }

    public abstract String getDefaultDisplayName();

    public abstract List<String> getDefaultDescription();

    public Map<String, Object> getReplaceable() {
        return Collections.emptyMap();
    }

    public abstract List<Pair<UUID, String>> getTopList();

    public abstract String getValue(UUID uuid);

    public List<String> getTopDescription() {
        return CollectionUtils.createStringListFromObject(get("top-description"), false);
    }

    public Arena getArena() {
        return arena;
    }

    public GamesInTheBox getInstance() {
        return instance;
    }
}
