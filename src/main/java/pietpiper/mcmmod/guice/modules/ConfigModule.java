package pietpiper.mcmmod.guice.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.fabricmc.loader.api.FabricLoader;
import pietpiper.mcmmod.config.AppConfig;
import pietpiper.mcmmod.config.converters.ColorDeserializer;
import pietpiper.mcmmod.config.converters.ColorSerializer;
import pietpiper.mcmmod.config.readers.ServerSettingsReader;
import pietpiper.mcmmod.config.readers.SkillConfigReader;
import pietpiper.mcmmod.config.server.ServerSettings;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;
import pietpiper.mcmmod.config.skill.SkillConfigs;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import static pietpiper.mcmmod.constants.ModConstants.MOD_ID;

/** Guice module for mod configuration components. **/
public class ConfigModule extends AbstractModule {

    /**
     * Provides the config mapper for serializing and deserializing.
     *
     * @return The {@link ObjectMapper} for configs.
     */
    @Provides
    @Singleton
    public ObjectMapper providerConfigMapper() {
        final ObjectMapper configMapper =
                new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                        .findAndRegisterModules();

        final SimpleModule module = new SimpleModule();
        module.addSerializer(Color.class, new ColorSerializer());
        module.addDeserializer(Color.class, new ColorDeserializer());
        configMapper.registerModule(module);

        return configMapper;
    }

    /**
     * Provides the {@link ServerSettings}.
     *
     * @param serverSettingsReader The {@link ServerSettingsReader} to read settings.
     * @return The loaded {@link ServerSettings}.
     * @throws IOException If reading settings fails.
     */
    @Provides
    @Singleton
    public ServerSettings provideServerSettings(
            @NonNull final ServerSettingsReader serverSettingsReader) throws IOException {
        return serverSettingsReader.read();
    }

    /**
     * Provides a map of all {@link Skill} to {@link SkillConfig}.
     *
     * @param skillConfigReader The {@link SkillConfigReader} to read skill configs.
     * @return {@link Map} of all skill configurations.
     * @throws IOException If reading skill configs fails.
     */
    @Provides
    @Singleton
    public SkillConfigs provideSkillConfigs(
            @NonNull final SkillConfigReader skillConfigReader) throws IOException {
        return skillConfigReader.readAll();
    }

    /**
     * Provides the {@link AppConfig} the mod.
     *
     * @return {@link AppConfig} to the mod config directory.
     */
    @Provides
    @Singleton
    public AppConfig  provideConfigDirectory() {
        return AppConfig.builder()
                .configDirectory(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID))
                .build();
    }
}