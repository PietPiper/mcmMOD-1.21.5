package pietpiper.mcmmod.config.skill.defaults;

import lombok.experimental.UtilityClass;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;

import java.awt.*;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@UtilityClass
public class SkillDefaults {

    public Map<Skill, SkillConfig> defaultSkills() {
        final Map<Skill, SkillConfig> map = new EnumMap<>(Skill.class);
        for (final Skill skill : Skill.values()) {
            map.put(skill, skill.defaultConfig());
        }
        return Collections.unmodifiableMap(map);
    }

    public Color defaultColor(Skill skill) {
        return skill.defaultConfig().getColor();
    }
}
