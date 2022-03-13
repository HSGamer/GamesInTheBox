package me.hsgamer.gamesinthebox.feature.game;

import me.hsgamer.gamesinthebox.api.ArenaGame;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RewardFeature implements Feature {
    private final Map<Integer, List<String>> topCommands = new HashMap<>();
    private final List<String> defaultCommands = new ArrayList<>();
    private final ArenaGame arenaGame;
    private final int minPlayersToReward;

    public RewardFeature(ArenaGame arenaGame, Map<String, Object> value, int minPlayersToReward) {
        this.arenaGame = arenaGame;
        this.minPlayersToReward = minPlayersToReward;
        value.forEach((k, v) -> {
            if (k.equals("default")) {
                defaultCommands.addAll(CollectionUtils.createStringListFromObject(v, true));
            } else {
                int i;
                try {
                    i = Integer.parseInt(k);
                } catch (Exception e) {
                    return;
                }
                topCommands.put(i, CollectionUtils.createStringListFromObject(v, true));
            }
        });
    }

    public static RewardFeature of(ArenaGame arenaGame) {
        Map<String, Object> value = arenaGame.getValues("reward", false);
        int minPlayersToReward = arenaGame.getInstance("min-players-to-reward", 0, Number.class).intValue();
        return new RewardFeature(arenaGame, value, minPlayersToReward);
    }

    public void tryReward(List<UUID> topList) {
        if (!reward(topList)) {
            String notEnoughPlayerMessage = arenaGame.getInstance().getMessageConfig().getNotEnoughPlayersToReward().replace("{name}", arenaGame.getArena().getName());
            Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, notEnoughPlayerMessage));
        }
    }

    public boolean reward(List<UUID> topList) {
        if (topList.size() < minPlayersToReward) {
            return false;
        }
        for (int i = 0; i < topList.size(); i++) {
            int top = i + 1;
            UUID uuid = topList.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();
            if (name == null) continue;
            List<String> commands = new ArrayList<>(topCommands.getOrDefault(top, defaultCommands));
            commands.replaceAll(s ->
                    s.replace("{name}", name).replace("{top}", Integer.toString(top))
            );
            Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () -> commands.forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c)));
        }
        return true;
    }
}
