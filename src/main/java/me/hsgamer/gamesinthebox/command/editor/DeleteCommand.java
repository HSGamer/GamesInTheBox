package me.hsgamer.gamesinthebox.command.editor;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
            if (instance.getArenaManager().getArenaByName(args[0]).isPresent()) {
                instance.getArenaConfig().remove(args[0]);
                instance.getArenaConfig().save();
                instance.getArenaManager().reloadArena();
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
            } else {
                MessageUtils.sendMessage(sender, instance.getMessageConfig().getArenaNotFound());
            }
        } else if (args.length == 2) {
            Optional<ArenaGame> optional = instance.getArenaManager().getArenaByName(args[0])
                    .map(arena -> arena.getArenaFeature(GameFeature.class))
                    .flatMap(arenaGameFeature -> arenaGameFeature.getGame(args[1]));
            if (optional.isPresent()) {
                instance.getArenaConfig().remove(args[0] + ".settings." + args[1]);
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
            return instance.getArenaManager().getAllArenas().stream()
                    .map(Arena::getName)
                    .filter(s -> args[0].isEmpty() || s.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return instance.getArenaManager().getArenaByName(args[0])
                    .map(arena -> arena.getArenaFeature(GameFeature.class))
                    .map(GameFeature.ArenaGameFeature::getAvailableGames)
                    .orElse(Collections.emptyList());
        }
        return super.onTabComplete(sender, label, args);
    }
}
