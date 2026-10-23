package evandev.redelightmap.mixin;

import evandev.redelightmap.RedelightMapConfig;
import evandev.redelightmap.client.DarknessPostEffect;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Final
    @Shadow
    Minecraft minecraft;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void lowLightVision(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.level == null) {
            return;
        }
        if (RedelightMapConfig.trueDarkMode) {
            DarknessPostEffect.render(this.minecraft, deltaTracker.getGameTimeDeltaTicks());
        }
    }
}
