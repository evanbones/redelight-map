package evandev.redelightmap;

//? if fabric {
//? if >=1.20.1 {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import evandev.redelightmap.config.RedelightMapConfigScreen;

public final class RedelightMap implements ClientModInitializer, ModMenuApi {
    public static final String MOD_ID = "redelightmap";

    @Override
    public void onInitializeClient() {
        RedelightMapConfigScreen.load();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return RedelightMapConfigScreen.isAvailable()
            ? RedelightMapConfigScreen::create
            : null;
    }
}
//? }
//? } else if forge {
/*import net.minecraftforge.fml.common.Mod;
//? if >=1.20.1 {
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.client.ConfigScreenHandler;
import evandev.redelightmap.config.RedelightMapConfigScreen;
//? }

@Mod(RedelightMap.MOD_ID)
public final class RedelightMap {
    public static final String MOD_ID = "redelightmap";

    public RedelightMap() {
        //? if >=1.20.1 {
        RedelightMapConfigScreen.load();
        if (RedelightMapConfigScreen.isAvailable()) {
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (client, parent) -> RedelightMapConfigScreen.create(parent)
                )
            );
        }
        //? }
    }
}
*///? } else {
/*import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import evandev.redelightmap.config.RedelightMapConfigScreen;
//? if >=1.21.1 {
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
//? } else {
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;
//? }

@Mod(RedelightMap.MOD_ID)
public final class RedelightMap {
    public static final String MOD_ID = "redelightmap";

    public RedelightMap(ModContainer container) {
        RedelightMapConfigScreen.load();

        if (RedelightMapConfigScreen.isAvailable()) {
            //? if >=1.21.1 {
            container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mc, parent) -> RedelightMapConfigScreen.create(parent)
            );
            //? } else {
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (client, parent) -> RedelightMapConfigScreen.create(parent)
                )
            );
            //? }
        }
    }
}
*///? }
