package pietpiper.mcmmod.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.Yaml;
import pietpiper.mcmmod.util.ServerReference;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SkillConfigManager {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("skill_config.yml").toString());
    private static final TreeMap<Integer, String> fishingTreasureTiers = new TreeMap<>();
    private static final TreeMap<Integer, FishermansDietBonus> fishermansDietTiers = new TreeMap<>();
    private static final TreeMap<Integer, MasterAnglerBonus> masterAnglerRanks = new TreeMap<>();
    private static final TreeMap<Integer, String> magicHunterTiers = new TreeMap<>();
    private static int iceFishingLevel = 0;
    private static int boatBonusMinTicks = 0;
    private static int boatBonusMaxTicks = 0;

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

            if (fishing.containsKey("IceFishingLevel")) {
                iceFishingLevel = (int) fishing.get("IceFishingLevel");
            } else {
                iceFishingLevel = 0; // fallback default
            }

            // Load Fisherman's Diet tiers
            fishermansDietTiers.clear();
            Map<String, Map<String, Object>> dietSection = (Map<String, Map<String, Object>>) fishing.get("FishermansDietTiers");
            for (Map.Entry<String, Map<String, Object>> entry : dietSection.entrySet()) {
                int level = (int) entry.getValue().get("Level");
                int hunger = (int) entry.getValue().get("Hunger");
                float saturation = ((Number) entry.getValue().get("Saturation")).floatValue();
                fishermansDietTiers.put(level, new FishermansDietBonus(hunger, saturation));
            }

            // Load Master Angler ranks
            masterAnglerRanks.clear();
            Map<String, Map<String, Object>> masterSection = (Map<String, Map<String, Object>>) fishing.get("MasterAnglerRanks");
            for (Map.Entry<String, Map<String, Object>> entry : masterSection.entrySet()) {
                int level = (int) entry.getValue().get("Level");
                int minReduction = (int) entry.getValue().get("MinReduction");
                int maxReduction = (int) entry.getValue().get("MaxReduction");
                masterAnglerRanks.put(level, new MasterAnglerBonus(minReduction, maxReduction));
            }

            boatBonusMinTicks = (int) fishing.getOrDefault("BoatBonusReductionMinTicks", 0);
            boatBonusMaxTicks = (int) fishing.getOrDefault("BoatBonusReductionMaxTicks", 0);

            // Load Magic Hunter tier level requirements
            magicHunterTiers.clear();
            if (fishing.containsKey("MagicHunterTiers")) {
                Map<String, Object> magicTiers = (Map<String, Object>) fishing.get("MagicHunterTiers");
                for (Map.Entry<String, Object> entry : magicTiers.entrySet()) {
                    magicHunterTiers.put((Integer) entry.getValue(), entry.getKey());
                }
            }

            ServerReference.logConsole("Skill tier config loaded.");

        } catch (Exception e) {
            ServerReference.logConsole("Error loading skill_config.yml");
            e.printStackTrace();
        }
    }

    public static String getMagicHunterTier(int level) {
        Map.Entry<Integer, String> entry = magicHunterTiers.floorEntry(level);
        return entry != null ? entry.getValue() : null;
    }

    public static String getFishingTier(int level) {
        Map.Entry<Integer, String> entry = fishingTreasureTiers.floorEntry(level);
        return entry != null ? entry.getValue() : null;
    }

    public static int getIceFishingLevel() {
        return iceFishingLevel;
    }

    public static FishermansDietBonus getFishermansDietBonus(int level) {
        Map.Entry<Integer, FishermansDietBonus> entry = fishermansDietTiers.floorEntry(level);
        return entry != null ? entry.getValue() : new FishermansDietBonus(0, 0);
    }

    public static int getTotalMinWaitReduction(int level, boolean inBoat) {
        int reduction = masterAnglerRanks.entrySet().stream()
                .filter(e -> level >= e.getKey())
                .mapToInt(e -> e.getValue().minReduction)
                .sum();
        if (inBoat) reduction += boatBonusMinTicks;
        return reduction;
    }

    public static int getTotalMaxWaitReduction(int level, boolean inBoat) {
        int reduction = masterAnglerRanks.entrySet().stream()
                .filter(e -> level >= e.getKey())
                .mapToInt(e -> e.getValue().maxReduction)
                .sum();
        if (inBoat) reduction += boatBonusMaxTicks;
        return reduction;
    }

    private static void saveDefault() {
        String yaml = """
            # The settings for the fishing subskill unlock requirements and customization.
            Fishing:
            
              # The level at which you unlock each tier of treasure hunter.
              # For customization of each tiers treasure items and dropchances refer to fishing_treasures_config.yml
              TreasureHunterTiers:
                Tier_1: 10
                Tier_2: 15
                Tier_3: 25
                Tier_4: 50
                Tier_5: 75
                Tier_6: 100
                Tier_7: 150
                Tier_8: 200
                Tier_9: 300
                Tier_10: 500
            
              # Magic Hunter tier unlock levels
              MagicHunterTiers:
                Tier_1: 0
                Tier_2: 10
                Tier_3: 25
                Tier_4: 50
                Tier_5: 75
                Tier_6: 550
                Tier_7: 600
                Tier_8: 650
                Tier_9: 700
                Tier_10: 750
            
              # The level at which you unlock ice fishing.
              IceFishingLevel: 30

              # Fisherman's Diet subskill tier configuration
              # This is the amount of extra hunger and saturation points you will recieve.
              FishermansDietTiers:
                Tier_1:
                  Level: 10
                  Hunger: 1
                  Saturation: 1.0
                Tier_2:
                  Level: 25
                  Hunger: 2
                  Saturation: 2.0
                Tier_3:
                  Level: 50
                  Hunger: 3
                  Saturation: 3.0
                Tier_4:
                  Level: 75
                  Hunger: 4
                  Saturation: 4.0
                Tier_5:
                  Level: 100
                  Hunger: 5
                  Saturation: 5.0
            
              # Master Angler rank settings
              # Includes the min and max reduction to time a player can spend
              # waiting for a fish and unlock level for each rank. Rank reductions stack.
              # The sum of previous minimums must be less than sum of maximums for any given rank.
              MasterAnglerRanks:
                Rank_1:
                  Level: 10
                  MinReduction: 2
                  MaxReduction: 5
                Rank_2:
                  Level: 25
                  MinReduction: 3
                  MaxReduction: 7
                Rank_3:
                  Level: 50
                  MinReduction: 4
                  MaxReduction: 9
                Rank_4:
                  Level: 75
                  MinReduction: 5
                  MaxReduction: 11
                Rank_5:
                  Level: 100
                  MinReduction: 6
                  MaxReduction: 13
                Rank_6:
                  Level: 125
                  MinReduction: 7
                  MaxReduction: 15
                Rank_7:
                  Level: 150
                  MinReduction: 8
                  MaxReduction: 17
                Rank_8:
                  Level: 175
                  MinReduction: 9
                  MaxReduction: 19
                Rank_9:
                  Level: 200
                  MinReduction: 10
                  MaxReduction: 21
                Rank_10:
                  Level: 225
                  MinReduction: 11
                  MaxReduction: 23
            
              # The additional reduction a player gets when sitting in a boat.
              BoatBonusReductionMinTicks: 2
              BoatBonusReductionMaxTicks: 4
            """;

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(yaml);
            System.out.println("Default skill_config.yml created.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FishermansDietBonus {
        public final int hunger;
        public final float saturation;

        public FishermansDietBonus(int hunger, float saturation) {
            this.hunger = hunger;
            this.saturation = saturation;
        }
    }

    public static class MasterAnglerBonus {
        public final int minReduction;
        public final int maxReduction;

        public MasterAnglerBonus(int minReduction, int maxReduction) {
            this.minReduction = minReduction;
            this.maxReduction = maxReduction;
        }
    }
}


