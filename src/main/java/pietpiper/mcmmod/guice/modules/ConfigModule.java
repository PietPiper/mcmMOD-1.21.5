package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.NonNull;
import net.fabricmc.loader.api.FabricLoader;
import pietpiper.mcmmod.config.readers.ServerSettingsReader;
import pietpiper.mcmmod.config.readers.SkillConfigReader;
import pietpiper.mcmmod.config.server.ServerSettings;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static pietpiper.mcmmod.constants.ModConstants.MOD_ID;

/** Guice module for mod configuration components. **/
public class ConfigModule extends AbstractModule {

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
    public Map<Skill, SkillConfig> provideSkillConfigs(
            @NonNull final SkillConfigReader skillConfigReader) throws IOException {
        return skillConfigReader.readAll();
    }

    /**
     * Provides the config directory path for the mod.
     *
     * @return {@link Path} to the mod config directory.
     */
    @Provides
    @Singleton
    @Named("ConfigDirectory")
    public Path provideConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }
}