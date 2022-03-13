package me.hsgamer.gamesinthebox.command.admin;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.Permissions;
import me.hsgamer.gamesinthebox.feature.GameFeature;
import me.hsgamer.gamesinthebox.state.IdlingState;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetGameCommand extends SubCommand {
    private final GamesInTheBox instance;

    public SetGameCommand(GamesInTheBox instance) {
        super("setgame", "Set the current game of the arena", "/gitbadmin setgame <arena> <game>", Permissions.SET_GAME.getName(), true);
        this.instance = instance;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        String arenaName = args[0];
        String gameName = args[1];
        Optional<Arena> arenaOptional = instance.getArenaManager().getArenaByName(arenaName);
        if (arenaOptional.isEmpty()) {
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getArenaNotFound());
            return;
        }
        Arena arena = arenaOptional.get();
        if (arena.getState() != IdlingState.class) {
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getArenaNotIdling());
            return;
        }
        GameFeature.ArenaGameFeature gameFeature = arena.getArenaFeature(GameFeature.class);
        if (!gameFeature.isGameExist(gameName)) {
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getGameNotFound());
            return;
        }
        gameFeature.setCurrentGame(gameName);
        MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 2;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            return instance.getArenaManager().getAllArenas().stream().map(Arena::getName).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return instance.getArenaManager().getArenaByName(args[0])
                    .map(arena -> arena.getArenaFeature(GameFeature.class))
                    .map(GameFeature.ArenaGameFeature::getAvailableGames)
                    .orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }
}
