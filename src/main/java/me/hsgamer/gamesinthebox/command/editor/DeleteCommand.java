package me.hsgamer.gamesinthebox.command.editor;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.feature.EditorFeature;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteCommand extends SubCommand {
    private final GamesInTheBox instance;

    public DeleteCommand(GamesInTheBox instance) {
        super("delete", "Delete an arena game", "/gitbeditor delete <arena> [game-name]", null, true);
        this.instance = instance;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            if (instance.getArenaConfig().contains(args[0])) {
                instance.getArenaConfig().remove(args[0]);
                instance.getArenaConfig().save();
                instance.getArenaManager().reloadArena();
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
            } else {
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getArenaNotFound());
            }
        } else if (args.length == 2) {
            String path = args[0] + ".settings." + args[1];
            if (instance.getArenaConfig().contains(path)) {
                instance.getArenaConfig().remove(path);
                instance.getArenaConfig().save();
                instance.getArenaManager().reloadArena();
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
            } else {
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getGameNotFound());
            }
        }
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length > 0;
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
        }
        return super.onTabComplete(sender, label, args);
    }
}
