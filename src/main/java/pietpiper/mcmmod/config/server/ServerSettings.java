package pietpiper.mcmmod.config.server;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Settings for the server. **/
@Value
@Builder
@Jacksonized
public class ServerSettings {
    @Builder.Default int maxLevel = 10000;
    @Builder.Default int startingLevel = 0;
    @Builder.Default boolean showXpBar = true;
    @Builder.Default boolean enableActiveSkills = true;
    @Builder.Default boolean debugMode = false;
}
