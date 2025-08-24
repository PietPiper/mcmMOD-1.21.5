package pietpiper.mcmmod.config.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;
import pietpiper.mcmmod.config.McmmodConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class ConfigFileManager {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("MCMMOD");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("mcmmod.yml").toFile();
    private static final File DEFAULT_FILE = CONFIG_DIR.resolve("defaults/mcmmod_defaults.yml").toFile();

    public File getConfigFile() {
        return CONFIG_FILE;
    }

    /**
     * Save the default McmmodConfig to YAML
     * */
    public void saveDefault() throws IOException {
        McmmodConfig defaultConfig = McmmodConfig.builder().build();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .findAndRegisterModules();

        Files.createDirectories(CONFIG_FILE.getParentFile().toPath());
        Files.createDirectories(DEFAULT_FILE.getParentFile().toPath());

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            mapper.writeValue(writer, defaultConfig);
        }

        try (FileWriter writer = new FileWriter(DEFAULT_FILE)) {
            mapper.writeValue(writer, defaultConfig);
        }
    }
}
