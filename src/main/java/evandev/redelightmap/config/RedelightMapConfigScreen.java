package evandev.redelightmap.config;

//? if >=1.20.1 {
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
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
            if (json != null) {
                if (json.has("enabled"))
                    RedelightMapConfig.enabled = json.get("enabled").getAsBoolean();
                if (json.has("brightness"))
                    RedelightMapConfig.brightness = json.get("brightness").getAsFloat();
                if (json.has("moonPhaseDarkness"))
                    RedelightMapConfig.moonPhaseDarkness = json.get("moonPhaseDarkness").getAsFloat();
                if (json.has("trueDarkMode"))
                    RedelightMapConfig.trueDarkMode = json.get("trueDarkMode").getAsBoolean();
                if (json.has("lowLightVision"))
                    RedelightMapConfig.lowLightVision = json.get("lowLightVision").getAsBoolean();
                if (json.has("lowLightVisionDesaturation"))
                    RedelightMapConfig.lowLightVisionDesaturation = json.get("lowLightVisionDesaturation").getAsFloat();
                if (json.has("lowLightVisionBrightness"))
                    RedelightMapConfig.lowLightVisionBrightness = json.get("lowLightVisionBrightness").getAsFloat();
                if (json.has("lowLightVisionRange"))
                    RedelightMapConfig.lowLightVisionRange = json.get("lowLightVisionRange").getAsFloat();
                if (json.has("lowLightVisionDarkAdaptionTime"))
                    RedelightMapConfig.lowLightVisionDarkAdaptionTime = json.get("lowLightVisionDarkAdaptionTime").getAsFloat();
                if (json.has("lowLightVisionLightAdaptionTime"))
                    RedelightMapConfig.lowLightVisionLightAdaptionTime = json.get("lowLightVisionLightAdaptionTime").getAsFloat();
            }
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", RedelightMapConfig.enabled);
        json.addProperty("brightness", RedelightMapConfig.brightness);
        json.addProperty("moonPhaseDarkness", RedelightMapConfig.moonPhaseDarkness);
        json.addProperty("lowLightVision", RedelightMapConfig.lowLightVision);
        json.addProperty("trueDarkMode", RedelightMapConfig.trueDarkMode);
        json.addProperty("lowLightVisionDesaturation", RedelightMapConfig.lowLightVisionDesaturation);
        json.addProperty("lowLightVisionBrightness", RedelightMapConfig.lowLightVisionBrightness);
        json.addProperty("lowLightVisionRange", RedelightMapConfig.lowLightVisionRange);
        json.addProperty("lowLightVisionLightAdaptionTime", RedelightMapConfig.lowLightVisionLightAdaptionTime);
        json.addProperty("lowLightVisionDarkAdaptionTime", RedelightMapConfig.lowLightVisionDarkAdaptionTime);

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
                    .option(Option.<Float>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.brightness"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.brightness.description")))
                        .binding(1.0f, () -> RedelightMapConfig.brightness, value -> RedelightMapConfig.brightness = value)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0.0f, 1.0f)
                            .step(0.01f)
                            .formatValue(v -> Component.literal(Math.round(v * 100.0f) + "%")))
                        .build())
                    .option(Option.<Float>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.moonPhaseDarkness"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.moonPhaseDarkness.description")))
                        .binding(1.0f, () -> RedelightMapConfig.moonPhaseDarkness, value -> RedelightMapConfig.moonPhaseDarkness = value)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0.0f, 1.0f)
                            .step(0.01f)
                            .formatValue(v -> Component.literal(Math.round(v * 100.0f) + "%")))
                        .build())
                    .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.trueDarkness"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.trueDarkness.description")))
                        .binding(true, () -> RedelightMapConfig.trueDarkMode, value -> RedelightMapConfig.trueDarkMode = value)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Component.translatable("redelightmap.config.group.lowLightVision"))
                    .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.lowLightVision"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.lowLightVision.description")))
                        .binding(true, () -> RedelightMapConfig.lowLightVision, value -> RedelightMapConfig.lowLightVision = value)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                    .option(Option.<Float>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.lowLightVisionRange"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.lowLightVisionRange.description")))
                        .binding(14.0f, () -> RedelightMapConfig.lowLightVisionRange, value -> RedelightMapConfig.lowLightVisionRange = value)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(3.0f, 32.0f)
                            .step(0.1f))
                        .build())
                    .option(Option.<Float>createBuilder()
                        .name(Component.translatable("redelightmap.config.option.lowLightVisionBrightness"))
                        .description(OptionDescription.of(Component.translatable("redelightmap.config.option.lowLightVisionBrightness.description")))
                        .binding(4.0f, () -> RedelightMapConfig.lowLightVisionBrightness, value -> RedelightMapConfig.lowLightVisionBrightness = value)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(1.0f, 10.0f)
                                .step(0.1f))
                        .build())
                    .option(Option.<Float>createBuilder()
                            .name(Component.translatable("redelightmap.config.option.lowLightVisionDesaturation"))
                            .description(OptionDescription.of(Component.translatable("redelightmap.config.option.lowLightVisionDesaturation.description")))
                            .binding(0.75f, () -> RedelightMapConfig.lowLightVisionDesaturation, value -> RedelightMapConfig.lowLightVisionDesaturation = value)
                            .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                    .range(0.0f, 1.0f)
                                    .step(0.01f)
                                    .formatValue(v -> Component.literal(Math.round(v * 100.0f) + "%")))
                            .build())
                    .option(Option.<Float>createBuilder()
                            .name(Component.translatable("redelightmap.config.option.lowLightVisionDarkAdaptionTime"))
                            .description(OptionDescription.of(Component.translatable("redelightmap.config.option.lowLightVisionDarkAdaptionTime.description")))
                            .binding(3.0f, () -> RedelightMapConfig.lowLightVisionDarkAdaptionTime, value -> RedelightMapConfig.lowLightVisionDarkAdaptionTime = value)
                            .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                    .range(0.0f, 10.0f)
                                    .step(0.1f)
                                    .formatValue(v -> Component.literal(v + "s")))
                            .build())
                    .option(Option.<Float>createBuilder()
                            .name(Component.translatable("redelightmap.config.option.lowLightVisionLightAdaptionTime"))
                            .description(OptionDescription.of(Component.translatable("redelightmap.config.option.lowLightVisionLightAdaptionTime.description")))
                            .binding(0.6f, () -> RedelightMapConfig.lowLightVisionLightAdaptionTime, value -> RedelightMapConfig.lowLightVisionLightAdaptionTime = value)
                            .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                    .range(0.0f, 10.0f)
                                    .step(0.1f)
                                    .formatValue(v -> Component.literal(v + "s")))
                            .build())
                    .build())
                .build())
            .save(RedelightMapConfigScreen::save)
            .build()
            .generateScreen(parent);
    }
}
//? }
