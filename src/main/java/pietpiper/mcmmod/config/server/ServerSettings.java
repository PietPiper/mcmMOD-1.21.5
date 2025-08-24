package pietpiper.mcmmod.config.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerSettings {
    @Builder.Default int maxLevel = 10000;
    @Builder.Default int startingLevel = 0;
    @Builder.Default boolean showXpBar = true;
    @Builder.Default boolean enableActiveSkills = true;
    @Builder.Default boolean debugMode = false;
}
