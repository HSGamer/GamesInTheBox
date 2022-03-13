package me.hsgamer.gamesinthebox.config;

import me.hsgamer.hscore.config.annotation.ConfigPath;

public interface MainConfig {
    void reloadConfig();

    @ConfigPath("time-format")
    default String getTimeFormat() {
        return "HH:mm:ss";
    }

    @ConfigPath("max-top-display")
    default int getMaxTopDisplay() {
        return 5;
    }
}
