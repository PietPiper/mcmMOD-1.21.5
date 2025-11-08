package pietpiper.mcmmod.config.skill;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import pietpiper.mcmmod.config.converters.ColorDeserializer;
import pietpiper.mcmmod.config.converters.ColorSerializer;

import java.awt.Color;

/** A {@link Skill}s config. **/
@Value
@Builder
@Jacksonized
public class SkillConfig {
    boolean enabled;

    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    Color color;
}
