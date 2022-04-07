package me.hsgamer.gamesinthebox.command;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.Permissions;
import me.hsgamer.gamesinthebox.command.editor.CreateCommand;
import me.hsgamer.gamesinthebox.command.editor.DeleteCommand;
import me.hsgamer.gamesinthebox.command.editor.EditCommand;
import me.hsgamer.hscore.bukkit.command.sub.SubCommandManager;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EditorCommand extends Command {
    private final SubCommandManager subCommandManager;

    public EditorCommand(GamesInTheBox instance) {
        super("gitbeditor", "Editor Command", "/gitbeditor", Collections.emptyList());
        setPermission(Permissions.EDITOR.getName());
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
        subCommandManager.registerSubcommand(new CreateCommand(instance));
        subCommandManager.registerSubcommand(new DeleteCommand(instance));
        subCommandManager.registerSubcommand(new EditCommand(instance));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return testPermission(sender) && subCommandManager.onCommand(sender, commandLabel, args);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return subCommandManager.onTabComplete(sender, alias, args);
    }
}
