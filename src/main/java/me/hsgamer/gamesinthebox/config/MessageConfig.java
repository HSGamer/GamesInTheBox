package me.hsgamer.gamesinthebox.config;

import me.hsgamer.gamesinthebox.config.converter.StringListConverter;
import me.hsgamer.hscore.config.annotation.ConfigPath;

import java.util.List;

public interface MessageConfig {
    void reloadConfig();

    @ConfigPath("prefix")
    default String getPrefix() {
        return "&7[&cGamesInTheBox&7] &f";
    }

    @ConfigPath("point.add")
    default String getPointAdd() {
        return "&a+ {point} point(s) &7({total})";
    }

    @ConfigPath("point.minus")
    default String getPointMinus() {
        return "&c- {point} point(s) &7({total})";
    }

    @ConfigPath("success")
    default String getSuccess() {
        return "&aSuccess!";
    }

    @ConfigPath("arena-not-found")
    default String getArenaNotFound() {
        return "&cArena not found!";
    }

    @ConfigPath("game-not-found")
    default String getGameNotFound() {
        return "&cGame not found!";
    }

    @ConfigPath("arena-not-idling")
    default String getArenaNotIdling() {
        return "&cThe arena is not idling";
    }

    @ConfigPath("not-enough-players-to-reward")
    default String getNotEnoughPlayersToReward() {
        return "&cThe arena {name} does not have enough players to give rewards";
    }

    @ConfigPath("state.idling")
    default String getIdlingState() {
        return "&7Idling...";
    }

    @ConfigPath("state.waiting")
    default String getWaitingState() {
        return "&7Waiting for players...";
    }

    @ConfigPath("state.in-game")
    default String getInGameState() {
        return "&7Playing...";
    }

    @ConfigPath("state.ending")
    default String getEndingState() {
        return "&7Ending game...";
    }

    //region KOTH
    @ConfigPath("koth.name")
    default String getKOTHDisplayName() {
        return "&cKing of the Hill";
    }

    @ConfigPath(value = "koth.description", converter = StringListConverter.class)
    default List<String> getKOTHDescription() {
        return List.of("&7A game where you have to be the king of the hill to win");
    }

    @ConfigPath("koth.start-broadcast")
    default String getKOTHStartBroadcast() {
        return "&aThe arena {name} was started. Claim it!";
    }

    @ConfigPath("koth.end-broadcast")
    default String getKOTHEndBroadcast() {
        return "&eThe arena {name} was ended. Thanks!";
    }
    //endregion

    //region FFA
    @ConfigPath("ffa.name")
    default String getFFADisplayName() {
        return "&cFree For All";
    }

    @ConfigPath(value = "ffa.description", converter = StringListConverter.class)
    default List<String> getFFADescription() {
        return List.of("&7A game where you have to kill the other players");
    }

    @ConfigPath("ffa.start-broadcast")
    default String getFFAStartBroadcast() {
        return "&aThe arena {name} was started. Kill the other players!";
    }

    @ConfigPath("ffa.end-broadcast")
    default String getFFAEndBroadcast() {
        return "&eThe arena {name} was ended. Thanks!";
    }
    //endregion

    //region RUSH
    @ConfigPath("rush.name")
    default String getRushDisplayName() {
        return "&cBlock Rush";
    }

    @ConfigPath(value = "rush.description", converter = StringListConverter.class)
    default List<String> getRushDescription() {
        return List.of("&7A game where you have to mine the blocks");
    }

    @ConfigPath("rush.start-broadcast")
    default String getRushStartBroadcast() {
        return "&aThe arena {name} was started. Mine the blocks!";
    }

    @ConfigPath("rush.end-broadcast")
    default String getRushEndBroadcast() {
        return "&eThe arena {name} was ended. Thanks!";
    }
    //endregion
}
