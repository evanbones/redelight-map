package evandev.redelightmap.compat;

import com.mojang.blaze3d.platform.NativeImage;

import java.lang.reflect.Method;

public final class DistantHorizonsCompat {
    private static Object wrapperInstance = null;
    private static Method updateMethod = null;
    private static boolean failed = false;

    private DistantHorizonsCompat() {}

    public static void updateLightmap(NativeImage lightPixels) {
        if (failed || lightPixels == null) return;

        if (updateMethod == null || wrapperInstance == null) {
            try {
                Class<?> injectorClass = Class.forName("com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector");
                Object injector = injectorClass.getField("INSTANCE").get(null);
                Class<?> iface = Class.forName("com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper");
                wrapperInstance = injectorClass.getMethod("get", Class.class).invoke(injector, iface);

                if (wrapperInstance == null) {
                    return;
                }

                for (Method m : wrapperInstance.getClass().getMethods()) {
                    if (m.getName().equals("updateLightmap") && m.getParameterCount() == 1
                            && m.getParameterTypes()[0].isAssignableFrom(lightPixels.getClass())) {
                        m.setAccessible(true);
                        updateMethod = m;
                        break;
                    }
                }

                if (updateMethod == null) {
                    failed = true;
                    return;
                }
            } catch (Throwable t) {
                failed = true;
                return;
            }
        }

        try {
            updateMethod.invoke(wrapperInstance, lightPixels);
        } catch (Throwable t) {
            failed = true;
        }
    }
}
