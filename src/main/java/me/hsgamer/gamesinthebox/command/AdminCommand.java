package me.hsgamer.gamesinthebox.command;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.command.admin.ReloadCommand;
import me.hsgamer.gamesinthebox.command.admin.SetGameCommand;
import me.hsgamer.gamesinthebox.command.admin.SkipTimeCommand;
import me.hsgamer.hscore.bukkit.command.sub.SubCommandManager;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AdminCommand extends Command {
    private final SubCommandManager subCommandManager;

    public AdminCommand(GamesInTheBox instance) {
        super("gitbadmin", "Admin Command", "/gitbadmin", Collections.emptyList());
        subCommandManager = new SubCommandManager() {
            @Override
            public void sendHelpMessage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
                getSubcommands().forEach((name, subCommand) -> MessageUtils.sendMessage(sender, "&f/" + label + " " + name + ": &e" + subCommand.getDescription()));
            }

            @Override
            public void sendArgNotFoundMessage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
                MessageUtils.sendMessage(sender, "&cInvalid Arguments");
            }
        };
        subCommandManager.registerSubcommand(new SkipTimeCommand(instance));
        subCommandManager.registerSubcommand(new ReloadCommand(instance));
        subCommandManager.registerSubcommand(new SetGameCommand(instance));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return subCommandManager.onCommand(sender, commandLabel, args);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return subCommandManager.onTabComplete(sender, alias, args);
    }
}
