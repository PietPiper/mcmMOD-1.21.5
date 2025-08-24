package pietpiper.mcmmod.config.skill;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import pietpiper.mcmmod.config.converter.ColorDeserializer;
import pietpiper.mcmmod.config.converter.ColorSerializer;

import java.awt.Color;

@Value
@Builder
public class SkillConfig {
    boolean enabled;

    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    Color color;
}
