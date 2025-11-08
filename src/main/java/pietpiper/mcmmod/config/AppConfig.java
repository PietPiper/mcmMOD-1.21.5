package pietpiper.mcmmod.config;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

/** Config for the mod itself. */
@Value
@Builder
public class AppConfig {
  Path configDirectory;
}
