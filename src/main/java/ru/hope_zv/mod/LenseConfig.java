package ru.hope_zv.mod;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import lombok.Getter;
import lombok.Setter;

public class LenseConfig {
    public static final BuilderCodec<LenseConfig> CODEC;

    public static final float HUD_SCALE_MIN = 0.25f;
    public static final float HUD_SCALE_MAX = 3.0f;

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

        builder.append(
                        new KeyedCodec<>("ServerDefaultHudScale", Codec.FLOAT),
                        LenseConfig::setServerDefaultHudScale,
                        LenseConfig::getServerDefaultHudScale
                )
                .addValidator(Validators.range(LenseConfig.HUD_SCALE_MIN, LenseConfig.HUD_SCALE_MAX))
                .documentation("Default scale multiplier for the Lense HUD")
                .add();

        CODEC = builder.build();
    }

    @Setter
    @Getter
    private boolean serverDefaultHudEnabled = true;

    @Setter
    @Getter
    private float serverDefaultHudScale = 1f;

}
