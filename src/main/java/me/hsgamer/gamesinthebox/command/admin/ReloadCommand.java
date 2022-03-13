package me.hsgamer.gamesinthebox.command.admin;

import me.hsgamer.gamesinthebox.GamesInTheBox;
import me.hsgamer.gamesinthebox.Permissions;
import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends SubCommand {
    private final GamesInTheBox instance;

    public ReloadCommand(GamesInTheBox instance) {
        super("reload", "Reload the plugin", "/gitbadmin reload", Permissions.RELOAD.getName(), true);
        this.instance = instance;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        instance.getMainConfig().reloadConfig();
        instance.getMessageConfig().reloadConfig();
        instance.getArenaConfig().reload();
        instance.getArenaManager().reloadArena();
        MessageUtils.sendMessage(sender, instance.getMessageConfig().getSuccess());
    }
}
