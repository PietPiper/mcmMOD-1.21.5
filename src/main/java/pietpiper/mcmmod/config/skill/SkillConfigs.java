package pietpiper.mcmmod.config.skill;

import com.google.inject.Singleton;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Singleton
@Value
@Builder
public class SkillConfigs {
  Map<Skill, SkillConfig> skillConfigs;
}