package me.hsgamer.gamesinthebox.api.editor;

import me.hsgamer.gamesinthebox.util.LocationUtils;
import me.hsgamer.hscore.common.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class Editors {
    private Editors() {
        // EMPTY
    }

    public static SimpleValueArenaGameEditor<Number> ofNumber(String path) {
        return new SimpleValueArenaGameEditor<>(path, args -> {
            if (args.length == 0) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
            try {
                return Pair.of(EditorResponse.SUCCESS, Optional.of(Double.parseDouble(args[0])));
            } catch (NumberFormatException e) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
        });
    }

    public static SimpleValueArenaGameEditor<String> ofString(String path) {
        return new SimpleValueArenaGameEditor<>(path, args -> {
            if (args.length == 0) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
            return Pair.of(EditorResponse.SUCCESS, Optional.of(String.join(" ", args)));
        });
    }

    public static SimpleValueArenaGameEditor<Boolean> ofBoolean(String path) {
        return new SimpleValueArenaGameEditor<>(path, args -> {
            if (args.length == 0) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
            return Pair.of(EditorResponse.SUCCESS, Optional.of(Boolean.parseBoolean(args[0])));
        });
    }

    public static SimpleValueArenaGameEditor<TimeUnit> ofTimeUnit(String path) {
        return new SimpleValueArenaGameEditor<>(path, args -> {
            if (args.length == 0) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
            try {
                return Pair.of(EditorResponse.SUCCESS, Optional.of(TimeUnit.valueOf(args[0].toUpperCase(Locale.ROOT))));
            } catch (NumberFormatException e) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
        });
    }

    public static SimpleValueArenaGameEditor<String> ofLookingBlock(String path, boolean withWorld) {
        return new SimpleValueArenaGameEditor<>(path, (sender, args) -> {
            if (!(sender instanceof Player)) {
                return Pair.of(EditorResponse.PLAYER_ONLY, Optional.empty());
            }
            Player player = (Player) sender;
            Block block = player.getTargetBlock(null, 5);
            if (block.getType() == Material.AIR) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
            Location location = block.getLocation();
            String value = LocationUtils.serializeLocation(location, withWorld, true);
            return Pair.of(EditorResponse.SUCCESS, Optional.of(value));
        });
    }

    public static SimpleValueArenaGameEditor<String> ofCurrentPosition(String path, boolean withWorld, boolean roundNumbers) {
        return new SimpleValueArenaGameEditor<>(path, (sender, args) -> {
            if (!(sender instanceof Player)) {
                return Pair.of(EditorResponse.PLAYER_ONLY, Optional.empty());
            }
            Player player = (Player) sender;
            Location location = player.getLocation();
            String value = LocationUtils.serializeLocation(location, withWorld, roundNumbers);
            return Pair.of(EditorResponse.SUCCESS, Optional.of(value));
        });
    }

    public static SimpleValueArenaGameEditor<Map<String, String>> ofMap(String path, String separator) {
        return new SimpleValueArenaGameEditor<>(path, (sender, args) -> {
            String combined = String.join(" ", args);
            String[] split = combined.split(separator);
            if (split.length % 2 != 0) {
                return Pair.of(EditorResponse.INVALID_FORMAT, Optional.empty());
            }
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < split.length; i += 2) {
                map.put(split[i], split[i + 1]);
            }
            return Pair.of(EditorResponse.SUCCESS, Optional.of(map));
        });
    }

    public static SimpleValueArenaGameEditor<List<String>> ofList(String path, String separator) {
        return new SimpleValueArenaGameEditor<>(path, (sender, args) -> {
            String combined = String.join(" ", args);
            String[] split = combined.split(separator);
            return Pair.of(EditorResponse.SUCCESS, Optional.of(Arrays.asList(split)));
        });
    }

    public static SimpleValueArenaGameEditor<String> ofWorld(String path) {
        return new SimpleValueArenaGameEditor<>(path, (sender, args) -> {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    return Pair.of(EditorResponse.SUCCESS, Optional.of(player.getWorld().getName()));
                } else {
                    return Pair.of(EditorResponse.PLAYER_ONLY, Optional.empty());
                }
            }
            return Pair.of(EditorResponse.SUCCESS, Optional.of(args[0]));
        });
    }
}
