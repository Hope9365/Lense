package ru.hope_zv.mod;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import lombok.Setter;

public class LenseConfig {
    public static final BuilderCodec<LenseConfig> CODEC;

    static {
        BuilderCodec.Builder<LenseConfig> builder = BuilderCodec.builder(
                LenseConfig.class,
                LenseConfig::new
        );

        builder.append(
                        new KeyedCodec<>("ServerDefaultHudEnabled", Codec.BOOLEAN),
                        LenseConfig::setServerDefaultHudEnabled,
                        LenseConfig::isServerDefaultHudEnabled
                )
                .documentation("Whether the Lense HUD is enabled by default on server")
                .add();

        CODEC = builder.build();
    }

    @Setter
    @Getter
    private boolean serverDefaultHudEnabled = true;

}
