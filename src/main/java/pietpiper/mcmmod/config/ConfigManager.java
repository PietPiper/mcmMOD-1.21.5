package pietpiper.mcmmod.config;

import net.fabricmc.loader.api.FabricLoader;
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
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("mcmmod.yml").toString());
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
            e.printStackTrace();
            config = new McmmodConfig(); // fallback
        }
    }

    public static void saveDefault() {
        String defaultYaml = """
        # Mcmmod Configuration
        # This file controls global settings for the mod

        # Default colors for each skill in hex (e.g., 0x00FFAA).
        # Used for the XP bars, scoreboards and other GUI elements.
        # Players can also individually customize these for themselves!
        defaultFishingColor: 0x00FFFF
        defaultTamingColor: 0xFFAA00
        defaultMiningColor: 0xAAAAAA
        defaultAcrobaticsColor: 0xFFFFFF
        defaultWoodcuttingColor: 0x00AA00
        defaultHerbalismColor: 0x55FF55
        defaultExcavationColor: 0xFFFF55
        defaultUnarmedColor: 0xAA00AA
        defaultArcheryColor: 0xAA00AA
        defaultSwordsColor: 0xAA0000
        defaultAxesColor: 0x00AAAA
        defaultAlchemyColor: 0xFF55FF
        defaultSmeltingColor: 0x0000AA
        defaultEnchantingColor: 0x5555FF
        defaultGlidingColor: 0x000000

        #==== Global Server Settings ====
        
        # Maximum skill level players can reach
        maxLevel: 10000
        
        # Whether the XP bar is shown to players. (TODO: Add for individual skills)
        showXpBar: true

        # Whether or not active skills are enabled on the server.
        enableActiveSkills: true
        
        # Level players start at for all skills.
        startingLevel: 0
        
        # Whether each skill is enabled on the server.
        enableFishing: true
        enableTaming: true
        enableMining: true
        enableAcrobatics: true
        enableWoodcutting: true
        enableHerbalism: true
        enableExcavation: true
        enableUnarmed: true
        enableArchery: true
        enableSwords: true
        enableAxes: true
        enableAlchemy: true
        enableSmelting: true
        enableEnchanting: true
        enableGliding: true
        
        
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
