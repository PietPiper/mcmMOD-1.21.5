package pietpiper.mcmmod.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;


public class ConfigManager {
    private static final File CONFIG_FILE = new File("config/mcmmod.yml");
    private static McmmodConfig config;

    public static void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
            }
            try (InputStream input = Files.newInputStream(CONFIG_FILE.toPath())) {
                LoaderOptions options = new LoaderOptions();
                Constructor constructor = new Constructor(McmmodConfig.class, options);
                Yaml yaml = new Yaml(constructor);
                config = yaml.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
            config = new McmmodConfig(); // fallback
        }
    }

    public static void saveDefault() {
        String defaultYaml = """
        # Mcmmod Configuration
        # This file controls global settings for the mod

        # Default XP bar color in hex (e.g., 0x00FFAA) (currently not in hex but I need to make some changes to make sure it works);
        defaultXpBarColor: 65450

        # Whether the XP bar is shown to players
        showXpBar: true

        # Maximum skill level players can reach
        maxLevel: 10000
        """;

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(defaultYaml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static McmmodConfig getConfig() {
        return config;
    }
}
