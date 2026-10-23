package evandev.redelightmap.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import evandev.redelightmap.RedelightMap;
import evandev.redelightmap.RedelightMapConfig;
import evandev.redelightmap.mixin.GameRendererAccessor;
import evandev.redelightmap.mixin.PostChainAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.io.IOException;

public class DarknessPostEffect {
    private static final LazyChain DARKNESS = new LazyChain(ResourceLocation.fromNamespaceAndPath(RedelightMap.MOD_ID, "shaders/post/darkness.json"));

    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static NativeImage downsampledScreenPixel;
    private static float LowLightVisionAmount = 0.0f;
    private static RenderTarget opaqueDepthTarget;

    public static void render(Minecraft minecraft, float tickDelta) {
        resizeIfNeeded(minecraft);
        PostChain darkness = DARKNESS.get(minecraft);
        if (darkness == null) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        RenderTarget renderTarget = minecraft.getMainRenderTarget();
        double fovDegrees = ((GameRendererAccessor) minecraft.gameRenderer).redelightmap$getFov(camera, tickDelta, true);
        float tanHalfFovY = (float) Math.tan(Math.toRadians(fovDegrees) / 2.0);
        float tanHalfFovX = tanHalfFovY * ((float) renderTarget.width / renderTarget.height);

        if (RedelightMapConfig.lowLightVision) {
            float screenBrightness = measureScreenBrightness(darkness);
            float brightScreenThreshold = 0.05f;
            float targetLowLightAmount = 0.0f;
            float adaptationRate = 0.01f;
            float viewBrightness = ViewBrightnessSampler.getAverage(minecraft.level, camera, tanHalfFovX, tanHalfFovY);
            float FULL_VISION_LIGHT = 3.0f;
            float NO_VISION_LIGHT = 5.0f;

            if (screenBrightness > brightScreenThreshold) {
                targetLowLightAmount = 0.0f;
                adaptationRate =  screenBrightness;
            } else if (!Float.isNaN(viewBrightness)) {
                targetLowLightAmount = Mth.clamp((NO_VISION_LIGHT - viewBrightness) / (NO_VISION_LIGHT - FULL_VISION_LIGHT), 0.0f,1.0f);
                adaptationRate = targetLowLightAmount < LowLightVisionAmount ? viewBrightness / 50.0f : 0.01f;
            }
            float blend = 1.0f - (float) Math.exp(-adaptationRate * tickDelta);
            LowLightVisionAmount += (targetLowLightAmount - LowLightVisionAmount) * blend;
        } else {
            LowLightVisionAmount = 0.0f;
        }

        for (PostPass pass : ((PostChainAccessor) darkness).getPasses()) {
            pass.getEffect().safeGetUniform("FovProj").set(tanHalfFovX, tanHalfFovY);
            pass.getEffect().safeGetUniform("LowLightVisionAmount").set(LowLightVisionAmount);
        }

        if (opaqueDepthTarget != null) {
            PostPass pass = ((PostChainAccessor) darkness).getPasses().getFirst();
            pass.getEffect().setSampler("OpaqueDepthSampler", opaqueDepthTarget::getDepthTextureId);
        }

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        darkness.process(tickDelta);
        RenderSystem.enableDepthTest();
        minecraft.getMainRenderTarget().bindWrite(true);
    }

    private static void resizeIfNeeded(Minecraft minecraft) {
        RenderTarget main = minecraft.getMainRenderTarget();
        if (main.width == lastWidth && main.height == lastHeight) {
            return;
        }
        lastWidth = main.width;
        lastHeight = main.height;
        DARKNESS.resize(main.width, main.height);
    }

    private static void fixPassProjections(PostChain chain) {
        for (PostPass pass : ((PostChainAccessor) chain).getPasses()) {
            pass.setOrthoMatrix(new Matrix4f().setOrtho(0.0F, pass.outTarget.width, 0.0F, pass.outTarget.height, 0.1F, 1000.0F));
        }
    }

    private static float measureScreenBrightness(PostChain chain) {
        RenderTarget measure = chain.getTempTarget("measure");
        if (downsampledScreenPixel == null) {
            downsampledScreenPixel = new NativeImage(1, 1, false);
        }
        RenderSystem.bindTexture(measure.getColorTextureId());
        downsampledScreenPixel.downloadTexture(0, false);
        int pixel = downsampledScreenPixel.getPixelRGBA(0, 0);
        float r = (pixel & 0xFF) / 255.0f;
        float g = (pixel >> 8 & 0xFF) / 255.0f;
        float b = (pixel >> 16 & 0xFF) / 255.0f;
        return r * 0.3333f + g * 0.3333f + b * 0.3333f;
    }

    private static final class LazyChain {
        private final ResourceLocation location;
        private PostChain chain;
        private boolean broken;

        LazyChain(ResourceLocation location) {
            this.location = location;
        }

        PostChain get(Minecraft minecraft) {
            if (broken) {
                return null;
            }
            if (chain == null) {
                try {
                    chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), location);
                    chain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
                    fixPassProjections(chain);
                } catch (IOException | JsonSyntaxException e) {
                    RedelightMap.LOG.error("Failed to load post-processing chain {}", location, e);
                    broken = true;
                    return null;
                }
            }
            return chain;
        }

        void resize(int width, int height) {
            if (chain != null) {
                chain.resize(width, height);
                fixPassProjections(chain);
            }
        }
    }

    public static void captureOpaqueDepth(Minecraft minecraft) {
        RenderTarget main = minecraft.getMainRenderTarget();

        if (opaqueDepthTarget == null) {
            opaqueDepthTarget = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        } else if (opaqueDepthTarget.width != main.width || opaqueDepthTarget.height != main.height) {
            opaqueDepthTarget.resize(main.width, main.height, Minecraft.ON_OSX);
        }

        opaqueDepthTarget.copyDepthFrom(main);
        main.bindWrite(false);
    }

    public static void closeOpaqueDepth() {
        if (opaqueDepthTarget != null) {
            opaqueDepthTarget.destroyBuffers();
            opaqueDepthTarget = null;
        }
    }
}
