package pietpiper.mcmmod.config.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pietpiper.mcmmod.config.AppConfig;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;
import pietpiper.mcmmod.config.skill.SkillConfigs;
import pietpiper.mcmmod.config.writers.SkillConfigWriter;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import static pietpiper.mcmmod.McmMod.log;
import static pietpiper.mcmmod.constants.ConfigConstants.SKILL_SETTINGS_DIRECTORY_NAME;

/** Reader for {@link SkillConfig}s. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SkillConfigReader {

   private final AppConfig appConfig;
  private final SkillConfigWriter skillConfigWriter;
  private final ObjectMapper configMapper;

  private Path getSkillsDir() {
    return appConfig.getConfigDirectory().resolve(SKILL_SETTINGS_DIRECTORY_NAME);
  }

  /**
   * Read all {@link SkillConfig} into a {@link SkillConfigs}.
   *
   * @return {@link SkillConfigs}.
   * @throws IOException If reading fails.
   */
  public SkillConfigs readAll() throws IOException {
    Files.createDirectories(getSkillsDir());

    final Map<Skill, SkillConfig> result = new EnumMap<>(Skill.class);

    for (Skill skill : Skill.values()) {
      final Path file = getSkillsDir().resolve(skill.name().toLowerCase() + ".yml");
      final SkillConfig cfg = readOrCreate(skill, file);
      result.put(skill, ensureDefaults(skill, cfg));
    }

    return SkillConfigs.builder()
            .skillConfigs(Map.copyOf(result))
            .build();
  }

  /**
   * Create a {@link SkillConfig} from a file, creating it if it doesn't exist.
   *
   * @param skill The {@link Skill} to read/create a config.
   * @param file The {@link Path} to the file.
   * @return The {@link SkillConfig} of the {@link Skill}.
   * @throws IOException If reading or creating fails.
   */
  private SkillConfig readOrCreate(@NonNull final Skill skill,
                                   @NonNull final Path file) throws IOException {
    if (!Files.exists(file)) {
      log.info("Config for {} does not exist, creating one", StringUtils.capitalize(skill.name()));
      skillConfigWriter.writeDefault(skill, file);
    }

    log.info("Reading {}", file.toFile().getName());
    return configMapper.readValue(file.toFile(), SkillConfig.class);
  }

  /**
   * Ensure a {@link Skill} has it's color set to its default in it's {@link SkillConfig}
   * if it was not able to be parsed from the config file.
   *
   * @param skill The {@link Skill} to ensure.
   * @param cfg The {@link SkillConfig} to set.
   * @return The ensured {@link SkillConfig}.
   */
  private SkillConfig ensureDefaults(@NonNull final Skill skill,
                                     @NonNull final SkillConfig cfg) {
    Color color = cfg.getColor();
    if (color == null) {
      log.info("Setting {} to its default color", skill.name().toLowerCase());
      color = skill.defaultColor();
    }

    return SkillConfig.builder()
            .enabled(cfg.isEnabled())
            .color(color)
            .build();
  }
}