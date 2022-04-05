package me.hsgamer.gamesinthebox;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.hsgamer.gamesinthebox.command.AdminCommand;
import me.hsgamer.gamesinthebox.config.MainConfig;
import me.hsgamer.gamesinthebox.config.MessageConfig;
import me.hsgamer.gamesinthebox.manager.GameArenaManager;
import me.hsgamer.gamesinthebox.util.ArenaUtils;
import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class GamesInTheBox extends BasePlugin {
    private final MainConfig mainConfig = ConfigGenerator.newInstance(MainConfig.class, new BukkitConfig(this, "config.yml"));
    private final MessageConfig messageConfig = ConfigGenerator.newInstance(MessageConfig.class, new BukkitConfig(this, "messages.yml"));
    private final BukkitConfig arenaConfig = new BukkitConfig(this, "arenas.yml");
    private final GameArenaManager arenaManager = new GameArenaManager(this);

    @Override
    public void load() {
        arenaConfig.setup();
        MessageUtils.setPrefix(messageConfig::getPrefix);
    }

    @Override
    public void enable() {
        Permissions.addPermissions();
        registerCommand(new AdminCommand(this));
    }

    @Override
    public void postEnable() {
        arenaManager.init();
        arenaManager.postInit();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            GamesInTheBox instance = this;
            PlaceholderExpansion expansion = new PlaceholderExpansion() {
                @Override
                public boolean persist() {
                    return true;
                }

                @Override
                public @NotNull String getIdentifier() {
                    return instance.getName().toLowerCase(Locale.ROOT);
                }

                @Override
                public @NotNull String getAuthor() {
                    return String.join(", ", instance.getDescription().getAuthors());
                }

                @Override
                public @NotNull String getVersion() {
                    return instance.getDescription().getVersion();
                }

                @Override
                public String onRequest(OfflinePlayer player, String params) {
                    if (params.startsWith("time_")) {
                        return arenaManager.getArenaByName(params.substring("time_".length())).map(ArenaUtils::getCooldown).orElse("");
                    }
                    if (params.startsWith("state_")) {
                        return arenaManager.getArenaByName(params.substring("state_".length())).map(ArenaUtils::getStateName).orElse("");
                    }
                    if (params.startsWith("current_game_")) {
                        return arenaManager.getArenaByName("current_game_").map(ArenaUtils::getCurrentGame).orElse("");
                    }
                    if (params.startsWith("top_name_")) {
                        String selector = params.substring("top_name_".length());
                        Pair<String, Integer> pair = ArenaUtils.parseSelector(selector);
                        return arenaManager.getArenaByName(pair.getKey()).flatMap(a -> ArenaUtils.getTopName(a, pair.getValue())).orElse(mainConfig.getNullTopName());
                    }
                    if (params.startsWith("top_value_")) {
                        String selector = params.substring("top_value_".length());
                        Pair<String, Integer> pair = ArenaUtils.parseSelector(selector);
                        return arenaManager.getArenaByName(pair.getKey()).flatMap(a -> ArenaUtils.getTopValue(a, pair.getValue())).orElse(mainConfig.getNullTopValue());
                    }
                    return null;
                }
            };
            expansion.register();
            addDisableFunction(expansion::unregister);
        }
    }

    @Override
    public void disable() {
        arenaManager.clear();
        Permissions.removePermissions();
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public BukkitConfig getArenaConfig() {
        return arenaConfig;
    }

    public GameArenaManager getArenaManager() {
        return arenaManager;
    }
}
