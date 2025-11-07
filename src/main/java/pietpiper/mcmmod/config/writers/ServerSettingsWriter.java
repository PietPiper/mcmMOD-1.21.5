package pietpiper.mcmmod.config.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.config.server.ServerSettings;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static pietpiper.mcmmod.constants.ConfigConstants.SERVER_SETTINGS_FILE_NAME;

/** Writer for {@link ServerSettings} to file. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ServerSettingsWriter {

  @Named("ConfigDirectory") private final Path configDirectory;
  private final ObjectMapper configMapper;

  private Path getFilePath() {
    return configDirectory.resolve(SERVER_SETTINGS_FILE_NAME);
  }

  /**
   * Writes the default server settings to a YAML file.
   *
   * @throws IOException If writing fails.
   */
  public void writeDefault() throws IOException {
    Path filePath = getFilePath();
    Files.createDirectories(filePath.getParent());
    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      configMapper.writeValue(writer, ServerSettings.builder().build());
    }
  }
}