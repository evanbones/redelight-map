package evandev.redelightmap;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class RedelightMapConfig {
    private RedelightMapConfig() {}

    public static boolean enabled = true;
    public static float brightness = 1.0f;
    public static float moonPhaseDarkness = 0.5f;
    public static boolean trueDarkMode = true;
    public static boolean lowLightVision = true;

    public static float getMoonPhaseMultiplier(ClientLevel level) {
        float time = level.getTimeOfDay(0);
        float nightTime = Mth.cos((time + 0.5f) * ((float)Math.PI * 2f)) * 2.0f + 0.25f;
        nightTime = Mth.clamp(nightTime, 0.0F, 1.0F);
        float quadMoonBrightness = 1.0f - (float) Math.pow(1.0f - level.getMoonBrightness(), 2.0f);
        float moonPhaseMultiplier = Mth.lerp(RedelightMapConfig.moonPhaseDarkness,1.0f, quadMoonBrightness);
        return Mth.lerp(nightTime, 1.0f, moonPhaseMultiplier);
    }
}
