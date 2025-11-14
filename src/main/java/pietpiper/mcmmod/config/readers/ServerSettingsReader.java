package pietpiper.mcmmod.config.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.config.AppConfig;
import pietpiper.mcmmod.config.server.ServerSettings;
import pietpiper.mcmmod.config.writers.ServerSettingsWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static pietpiper.mcmmod.McmMod.log;
import static pietpiper.mcmmod.constants.FileNames.SERVER_SETTINGS_FILE_NAME;

/** Reader for the {@link ServerSettings}. **/
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ServerSettingsReader {

  private final AppConfig appConfig;
  private final ServerSettingsWriter serverSettingsWriter;
  private final ObjectMapper configMapper;

  /**
   * Loads server settings from YAML, generating a default if missing.
   */
  public ServerSettings read() throws IOException {
    Files.createDirectories(getFilePath().getParent());
    if (!Files.exists(getFilePath())) {
      serverSettingsWriter.writeDefault();
    }
    log.info("Reading {}", SERVER_SETTINGS_FILE_NAME);
    return configMapper.readValue(getFilePath().toFile(), ServerSettings.class);
  }

  private Path getFilePath() {
    return appConfig.getConfigDirectory().resolve(SERVER_SETTINGS_FILE_NAME);
  }
}
