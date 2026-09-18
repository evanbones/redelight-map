package evandev.redelightmap.config;

//? if >=1.20.1 {
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import evandev.redelightmap.RedelightMapConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
 //? } else if forge {
/*import net.minecraftforge.fml.loading.FMLPaths;
*///? } else {
/*import net.neoforged.fml.loading.FMLPaths;
*///? }

public final class RedelightMapConfigScreen {
    private static final Gson GSON = new Gson();
    private static final String FILE_NAME = "redelightmap.json";

    private RedelightMapConfigScreen() {}

    public static boolean isAvailable() {
        try {
            Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib", false, RedelightMapConfigScreen.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path))
            return;

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null && json.has("enabled"))
                RedelightMapConfig.enabled = json.get("enabled").getAsBoolean();
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", RedelightMapConfig.enabled);

        try (Writer writer = Files.newBufferedWriter(configPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
        } catch (IOException ignored) {
        }
    }

    private static Path configPath() {
        //? if fabric {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        //? } else {
        /*return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        *///? }
    }

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("redelightmap.config.title"))
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("redelightmap.config.category.general"))
                .group(OptionGroup.createBuilder()
                    .name(Component.translatable("redelightmap.config.group.general"))
                    .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.enabled"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.enabled.description")))
                        .binding(true, () -> RedelightMapConfig.enabled, value -> RedelightMapConfig.enabled = value)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                    .build())
                .build())
            .save(RedelightMapConfigScreen::save)
            .build()
            .generateScreen(parent);
    }
}
//? }
