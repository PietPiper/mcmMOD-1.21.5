package pietpiper.mcmmod.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.Yaml;
import pietpiper.mcmmod.util.ServerReference;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;

public class SkillConfigManager {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("skill_config.yml").toString());
    private static final TreeMap<Integer, String> fishingTreasureTiers = new TreeMap<>();

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            saveDefault();
        }

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(CONFIG_FILE.toPath()))) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(reader);
            Map<String, Object> fishing = (Map<String, Object>) root.get("Fishing");
            Map<String, Object> tiers = (Map<String, Object>) fishing.get("TreasureHunterTiers");

            fishingTreasureTiers.clear();
            for (Map.Entry<String, Object> entry : tiers.entrySet()) {
                fishingTreasureTiers.put((Integer) entry.getValue(), entry.getKey());
            }

            ServerReference.logConsole("Skill tier config loaded.");

        } catch (Exception e) {
            ServerReference.logConsole("Error loading skill_config.yml");
            e.printStackTrace();
        }
    }

    public static String getFishingTier(int level) {
        Map.Entry<Integer, String> entry = fishingTreasureTiers.floorEntry(level);
        return entry != null ? entry.getValue() : null;
    }

    private static void saveDefault() {
        String yaml = """
            Fishing:
              TreasureHunterTiers:
                Tier_1: 0
                Tier_2: 10
                Tier_3: 25
                Tier_4: 50
                Tier_5: 75
                Tier_6: 100
                Tier_7: 150
                Tier_8: 200
                Tier_9: 300
                Tier_10: 500
            """;

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(yaml);
            ServerReference.logConsole("Default skill_config.yml created.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
