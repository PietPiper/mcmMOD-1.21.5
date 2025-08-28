package pietpiper.mcmmod.config;

import net.fabricmc.loader.api.FabricLoader;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import pietpiper.mcmmod.objects.McmmodConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import static pietpiper.mcmmod.McmMod.log;

public class ConfigManager {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("MCMMOD");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("mcmmod.yml").toFile();
    private static final File DEFAULT_FILE = CONFIG_DIR.resolve("defaults/mcmmod_defaults.yml").toFile();

    private static McmmodConfig config = null;

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
            log.error(e.getMessage());
            config = new McmmodConfig();
        }
    }

    public static void saveDefault() {
        String defaultYaml = """
        # Mcmmod Configuration
        # This file controls global settings for the mod
    
        # Default fishing color in hex (e.g., 0x00FFAA).
        defaultFishingColor: 0x00FFFF
    
        #==== Global Server Settings ====
    
        # Maximum skill level players can reach
        maxLevel: 10000
    
        # Level players start at for all skills.
        startingLevel: 0
    
        # Debug settings.
        debugMode: false
        """;

        try {
            if (!CONFIG_DIR.toFile().exists()) CONFIG_DIR.toFile().mkdirs();
            if (!DEFAULT_FILE.getParentFile().exists()) DEFAULT_FILE.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write(defaultYaml);
            }

            try (FileWriter writer = new FileWriter(DEFAULT_FILE)) {
                writer.write(defaultYaml);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static McmmodConfig getConfig() {
        return config;
    }
}

