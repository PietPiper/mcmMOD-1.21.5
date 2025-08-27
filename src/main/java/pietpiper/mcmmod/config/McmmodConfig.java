package pietpiper.mcmmod.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import pietpiper.mcmmod.config.server.ServerSettings;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;
import pietpiper.mcmmod.config.skill.defaults.SkillDefaults;

import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = McmmodConfig.McmmodConfigBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class McmmodConfig {
    @Builder.Default ServerSettings serverSettings = ServerSettings.builder().build();
    @Builder.Default Map<Skill, SkillConfig> skills = SkillDefaults.defaultSkills();
}