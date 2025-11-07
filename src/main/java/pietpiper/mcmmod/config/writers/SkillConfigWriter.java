package pietpiper.mcmmod.config.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.config.skill.SkillConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writer for {@link Skill} to file. **/
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SkillConfigWriter {

  private final ObjectMapper configMapper;

  /**
   * Writes the default {@link SkillConfig} for a given {@link Skill} to a file.
   *
   * @throws IOException If writing fails.
   */
  public void writeDefault(Skill skill, Path file) throws IOException {
    Files.createDirectories(file.getParent());
    try (FileWriter writer = new FileWriter(file.toFile())) {
      configMapper.writeValue(writer, skill.defaultConfig());
    }
  }
}
