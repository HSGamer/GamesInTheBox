package me.hsgamer.gamesinthebox.command.editor;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.editor.EditorFeatureResponse;
import me.hsgamer.gamesinthebox.builder.ArenaGameBuilder;
import me.hsgamer.gamesinthebox.feature.EditorFeature;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class CreateCommand extends SubCommand {
    private final GamesInTheBox instance;

    public CreateCommand(GamesInTheBox instance) {
        super("create", "Create the arena game", "/gitbeditor create <arena> <game-name> <game-type>", null, true);
        this.instance = instance;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Arena arena = instance.getArenaManager().getArenaByName(args[0]).orElseGet(() -> {
            Arena newArena = new Arena(args[0], instance.getArenaManager());
            instance.getArenaConfig().set(args[0] + ".pick-strategy", "random");
            return newArena;
        });
        EditorFeature.ArenaEditorFeature editorFeature = arena.getArenaFeature(EditorFeature.class);
        EditorFeatureResponse response = editorFeature.createEditingArenaGame(args[1], args[2]);
        switch (response) {
            case SUCCESS:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
                break;
            case GAME_EXISTED:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getEditorGameExisted());
                break;
            case TYPE_NOT_FOUND:
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getEditorTypeNotFound());
                break;
        }
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 3;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            return instance.getArenaManager().getFeature(EditorFeature.class).getArenaNames()
                    .stream()
                    .filter(s -> args[0].isEmpty() || s.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return ArenaGameBuilder.INSTANCE.getNameMap().keySet().stream()
                    .filter(s -> args[2].isEmpty() || s.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, label, args);
    }
}
