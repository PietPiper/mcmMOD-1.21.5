package pietpiper.mcmmod.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import pietpiper.mcmmod.config.McmmodConfig;
import pietpiper.mcmmod.config.io.ConfigLoader;
import pietpiper.mcmmod.config.server.ServerSettings;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;
import pietpiper.mcmmod.config.skill.defaults.SkillDefaults;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class ConfigModule extends AbstractModule {

    private final McmmodConfig config;

    public ConfigModule() throws IOException {
        this.config = ConfigLoader.load();
    }

    @Provides
    @Singleton
    public McmmodConfig provideMcmmodConfig() {
        return config;
    }

    @Provides
    @Singleton
    public ServerSettings provideServerSettings(McmmodConfig config) {
        return config.getServerSettings();
    }

    @Provides
    @Singleton
    public Map<Skill, SkillConfig> provideSkillConfigs() {
        final Map<Skill, SkillConfig> skillMap = new EnumMap<>(Skill.class);
        skillMap.putAll(SkillDefaults.defaultSkills());
        skillMap.putAll(config.getSkills());
        return Map.copyOf(skillMap);
    }
}
