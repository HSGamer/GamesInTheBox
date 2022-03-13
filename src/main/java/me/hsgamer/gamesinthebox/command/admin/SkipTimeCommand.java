package me.hsgamer.gamesinthebox.command.admin;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.Permissions;
import me.hsgamer.gamesinthebox.feature.CooldownFeature;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.minigamecore.base.Arena;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SkipTimeCommand extends SubCommand {
    private final GamesInTheBox instance;

    public SkipTimeCommand(GamesInTheBox instance) {
        super("skiptime", "Skip the time of the arena", "/gitbadmin skip <arena>", Permissions.SKIP_TIME.getName(), true);
        this.instance = instance;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Optional<Arena> optionalArena = instance.getArenaManager().getArenaByName(args[0]);
        if (optionalArena.isPresent()) {
            optionalArena.get().getArenaFeature(CooldownFeature.class).setDuration(5, TimeUnit.MILLISECONDS);
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
        } else {
            MessageUtils.sendMessage(sender, instance.getMessageConfig().getArenaNotFound());
        }
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 1;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            return instance.getArenaManager().getAllArenas().stream().map(Arena::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
