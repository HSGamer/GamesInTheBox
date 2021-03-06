package me.hsgamer.gamesinthebox.command.editor;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.api.editor.EditorResponse;
import me.hsgamer.gamesinthebox.feature.EditorFeature;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditCommand extends SubCommand {
    private final GamesInTheBox instance;

    public EditCommand(GamesInTheBox instance) {
        super("edit", "Edit a value in the arena game", "/gitbeditor edit <arena> <game-name> <is-common> <editor-key> [value]", null, true);
        this.instance = instance;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Optional<Arena> optionalArena = instance.getArenaManager().getFeature(EditorFeature.class).getArena(args[0]);
        if (optionalArena.isEmpty()) {
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getArenaNotFound());
            return;
        }
        Arena arena = optionalArena.get();

        Optional<ArenaGame> optionalGame = arena.getArenaFeature(GameFeature.class).getGame(args[1]);
        if (optionalGame.isEmpty()) {
            optionalGame = arena.getArenaFeature(EditorFeature.class).getGame(args[1]);
        }
        if (optionalGame.isEmpty()) {
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getGameNotFound());
            return;
        }
        ArenaGame game = optionalGame.get();

        boolean isCommon = Boolean.parseBoolean(args[2]);
        String key = args[3];

        EditorResponse response = game.edit(sender, key, isCommon, Arrays.copyOfRange(args, 4, args.length));
        switch (response) {
            case SUCCESS:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
                break;
            case NOT_FOUND:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getEditorNotFound());
                break;
            case INVALID_FORMAT:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getEditorInvalidFormat());
                break;
            case PLAYER_ONLY:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getEditorPlayerOnly());
                break;
            case FAILED:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getEditorFailed());
                break;
        }
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 4;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            return instance.getArenaManager().getFeature(EditorFeature.class).getArenaNames()
                    .stream()
                    .filter(s -> args[0].isEmpty() || s.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return instance.getArenaManager().getFeature(EditorFeature.class).getArena(args[0])
                    .map(arena -> arena.getArenaFeature(EditorFeature.class).getAllGames())
                    .orElse(Collections.emptyList());
        } else if (args.length == 3) {
            return List.of("true", "false");
        } else if (args.length == 4) {
            return instance.getArenaManager().getFeature(EditorFeature.class).getArena(args[0])
                    .flatMap(arena -> {
                        Optional<ArenaGame> optional = arena.getArenaFeature(GameFeature.class).getGame(args[1]);
                        if (optional.isEmpty()) {
                            optional = arena.getArenaFeature(EditorFeature.class).getGame(args[1]);
                        }
                        return optional;
                    })
                    .map(game -> game.getEditors().keySet().stream()
                            .filter(s -> args[3].isEmpty() || s.startsWith(args[3]))
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        }
        return super.onTabComplete(sender, label, args);
    }
}
