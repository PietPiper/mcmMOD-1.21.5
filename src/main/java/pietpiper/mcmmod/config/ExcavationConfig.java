package pietpiper.mcmmod.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;

public class ExcavationConfig {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("mcmmod");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("excavation_xp_config.yml").toFile();
    private static final File DEFAULT_FILE = CONFIG_DIR.resolve("defaults/excavation_xp_defaults.yml").toFile();

    private static final Map<Block, Integer> blockXpMap = new HashMap<>();

    public static void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
            }

            try (InputStream input = Files.newInputStream(CONFIG_FILE.toPath())) {
                Yaml yaml = new Yaml();
                Map<String, Map<String, Object>> data = yaml.load(input);

                blockXpMap.clear();
                for (String key : data.keySet()) {
                    Identifier id = Identifier.of(key);
                    Block block = Registries.BLOCK.get(id);
                    if (block != null && data.get(key).containsKey("xp")) {
                        int xp = (int) data.get(key).get("xp");
                        blockXpMap.put(block, xp);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load ExcavationXPConfig: " + e.getMessage());
        }
    }

    public static int getXPForBlock(Block block) {
        return blockXpMap.getOrDefault(block, 0);
    }

    private static void saveDefault() {
        String defaultYaml = """
        # Excavation XP Configuration
        # Define XP values for breaking each block below

        sand:
          xp: 2

        grass_block:
          xp: 5

        gravel:
          xp: 8

        dirt:
          xp: 10

        podzol:
          xp: 15

        rooted_dirt:
          xp: 20
        
        mycelium:
          xp: 30
        
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
        } catch (Exception e) {
            System.err.println("Failed to save default ExcavationXPConfig: " + e.getMessage());
        }
    }
}
