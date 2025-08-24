package pietpiper.mcmmod.config.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.experimental.UtilityClass;
import pietpiper.mcmmod.config.McmmodConfig;
import pietpiper.mcmmod.config.converter.ColorDeserializer;
import pietpiper.mcmmod.config.converter.ColorSerializer;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;
import pietpiper.mcmmod.config.skill.defaults.SkillDefaults;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@UtilityClass
public class ConfigLoader {

    /**
     * Loads an immutable McmmodConfig from YAML
     */
    public McmmodConfig load() throws IOException {
        final File configFile = ConfigFileManager.getConfigFile();

        if (!configFile.exists()) {
            ConfigFileManager.saveDefault();
        }

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final SimpleModule colorModule = new SimpleModule();
        colorModule.addDeserializer(Color.class, new ColorDeserializer());
        colorModule.addSerializer(Color.class, new ColorSerializer());
        mapper.registerModule(colorModule);

        final McmmodConfig config = mapper.readValue(configFile, McmmodConfig.class);
        final Map<Skill, SkillConfig> updatedSkills = new EnumMap<>(Skill.class);
        config.getSkills().forEach((skill, skillConfig) -> {
            final Color color = skillConfig.getColor() != null
                    ? skillConfig.getColor()
                    : SkillDefaults.defaultColor(skill);

            final SkillConfig newConfig = SkillConfig.builder()
                    .enabled(skillConfig.isEnabled())
                    .color(color)
                    .build();

            updatedSkills.put(skill, newConfig);
        });

        final Map<Skill, SkillConfig> immutableSkills = Map.copyOf(updatedSkills);
        return McmmodConfig.builder()
                .serverSettings(config.getServerSettings())
                .skills(immutableSkills)
                .build();
    }
}
