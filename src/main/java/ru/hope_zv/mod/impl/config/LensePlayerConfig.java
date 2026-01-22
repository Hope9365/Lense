package ru.hope_zv.mod.impl.config;

import lombok.Getter;
import lombok.Setter;
import ru.hope_zv.mod.LenseConfig;

@Getter
@Setter
public class LensePlayerConfig {

    private boolean hudEnabled = true;

    public LensePlayerConfig() {
    }

    public static LensePlayerConfig createWithDefaults(LenseConfig serverConfig) {
        LensePlayerConfig config = new LensePlayerConfig();
        config.hudEnabled = serverConfig.isServerDefaultHudEnabled();
        return config;
    }
    
}
