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

    @ConfigPath(value = "hologram-template", converter = StringListConverter.class)
    default List<String> getDefaultHologramTemplate() {
        return List.of(
                "&c&lDescription",
                "{description}",
                "",
                "&c&lTop Description",
                "{top-description}"
        );
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

    //region PINATA
    @ConfigPath("pinata.name")
    default String getPinataDisplayName() {
        return "&cPinata";
    }

    @ConfigPath(value = "pinata.description", converter = StringListConverter.class)
    default List<String> getPinataDescription() {
        return List.of("&7A game where you have to hit the pinata");
    }

    @ConfigPath("pinata.start-broadcast")
    default String getPinataStartBroadcast() {
        return "&aThe arena {name} was started. Hit the pinata!";
    }

    @ConfigPath("pinata.end-broadcast")
    default String getPinataEndBroadcast() {
        return "&eThe arena {name} was ended. Thanks!";
    }

    @ConfigPath(value = "pinata.name-tag", converter = StringListConverter.class)
    default List<String> getPinataNameTag() {
        return List.of("&c&lHIT ME!", "&c&lMORE!");
    }
    //endregion

    //region Shoot the bat
    @ConfigPath("shoot-the-bat.name")
    default String getShootTheBatDisplayName() {
        return "&cShoot the Bat";
    }

    @ConfigPath(value = "shoot-the-bat.description", converter = StringListConverter.class)
    default List<String> getShootTheBatDescription() {
        return List.of("&7A game where you have to shoot the bat");
    }

    @ConfigPath("shoot-the-bat.start-broadcast")
    default String getShootTheBatStartBroadcast() {
        return "&aThe arena {name} was started. Shoot the bat!";
    }

    @ConfigPath("shoot-the-bat.end-broadcast")
    default String getShootTheBatEndBroadcast() {
        return "&eThe arena {name} was ended. Thanks!";
    }

    @ConfigPath(value = "shoot-the-bat.name-tag", converter = StringListConverter.class)
    default List<String> getShootTheBatNameTag() {
        return List.of("&c&lSHOOT ME!", "&c&lMORE!");
    }
    //endregion

    //region Hit the block
    @ConfigPath("hit-the-block.name")
    default String getHitTheBlockDisplayName() {
        return "&cHit the Block";
    }

    @ConfigPath(value = "hit-the-block.description", converter = StringListConverter.class)
    default List<String> getHitTheBlockDescription() {
        return List.of("&7A game where you have to hit the block");
    }

    @ConfigPath("hit-the-block.start-broadcast")
    default String getHitTheBlockStartBroadcast() {
        return "&aThe arena {name} was started. Hit the block!";
    }

    @ConfigPath("hit-the-block.end-broadcast")
    default String getHitTheBlockEndBroadcast() {
        return "&eThe arena {name} was ended. Thanks!";
    }
    //endregion
}
