package evandev.redelightmap.client;

import evandev.redelightmap.RedelightMapConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LightLevelSampler {
    private static final int SAMPLE_GRID_COLS = 8;
    private static final int SAMPLE_GRID_ROWS = 5;
    private static final double SAMPLE_RANGE = 32.0;

    public static float getAverage(
        ClientLevel level,
        Camera camera,
        float tanHalfFovX,
        float tanHalfFovY
    ) {
        Vec3 origin = camera.getPosition();
        Vec3 forward = new Vec3(camera.getLookVector());
        Vec3 left = new Vec3(camera.getLeftVector());
        Vec3 up = new Vec3(camera.getUpVector());

        float totalBrightness = 0.0f;
        int validSamples = 0;

        float moonPhaseMultiplier = RedelightMapConfig.getMoonPhaseMultiplier(level);

        for (int row = 0; row < SAMPLE_GRID_ROWS; row++) {
            for (int col = 0; col < SAMPLE_GRID_COLS; col++) {
                double screenX = 2.0 * (col + 0.5) / SAMPLE_GRID_COLS - 1.0;
                double screenY = 1.0 - 2.0 * (row + 0.5) / SAMPLE_GRID_ROWS;

                Vec3 direction = forward.add(left.scale(-screenX * tanHalfFovX)).add(up.scale(screenY * tanHalfFovY)).normalize();
                Vec3 end = origin.add(direction.scale(SAMPLE_RANGE));

                BlockHitResult hitResult = level.clip(new ClipContext(origin, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera.getEntity()));
                BlockPos sampleBlockPos;

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    Vec3 hitFaceNormal = Vec3.atLowerCornerOf(hitResult.getDirection().getNormal());
                    Vec3 sampleLocation = hitResult.getLocation().add(hitFaceNormal.scale(0.01));
                    sampleBlockPos = BlockPos.containing(sampleLocation);
                } else {
                    sampleBlockPos = BlockPos.containing(end);
                }

                if (!level.isLoaded(sampleBlockPos)) {
                    continue;
                }

                int skyBrightness = (int) ((level.getBrightness(LightLayer.SKY, sampleBlockPos) - level.getSkyDarken()) * moonPhaseMultiplier);
                int blockBrightness = level.getBrightness(LightLayer.BLOCK, sampleBlockPos);

                totalBrightness += Math.max(skyBrightness, blockBrightness);
                validSamples++;
            }
        }

        return validSamples > 0 ? totalBrightness / validSamples : Float.NaN;
    }
}
