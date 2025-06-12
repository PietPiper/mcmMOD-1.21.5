package pietpiper.mcmmod.config;

import pietpiper.mcmmod.util.ServerReference;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FishingConfig {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("MCMMOD");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("fishing_treasure_config.yml").toFile();
    private static final File DEFAULT_FILE = CONFIG_DIR.resolve("defaults/fishing_treasure_config_defaults.yml").toFile();

    public static final Map<String, LootEntry> allLootEntries = new HashMap<>();
    public static final TreeMap<String, Map<String, Double>> tierDropRates = new TreeMap<>();
    public static final Map<String, Double> nonTreasureDropChances = new HashMap<>();
    public static final Map<String, Map<Identifier, Integer>> enchantmentRarityTable = new HashMap<>();
    public static final TreeMap<String, Map<String, Double>> enchantmentDropRatesByTier = new TreeMap<>();
    public static final Map<String, Integer> enchantXpByRarity = new HashMap<>();
    public static boolean xpPerEnchant = false;
    public static final Map<String, List<ShakeDrop>> shakeDropTables = new HashMap<>();

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveDefault();
        }

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(CONFIG_FILE.toPath()))) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(reader);

            // Load Items
            Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) root.get("Items");
            for (String itemId : items.keySet()) {
                Identifier id = Identifier.tryParse(itemId);
                if (id == null || !Registries.ITEM.containsId(id)) {
                    ServerReference.logConsole("Invalid item in config: " + itemId);
                    continue;
                }
                Item item = Registries.ITEM.get(id);
                Map<String, Object> props = items.get(itemId);
                int amount = ((Number) props.getOrDefault("Amount", 1)).intValue();
                int xp = ((Number) props.getOrDefault("XP", 0)).intValue();
                String rarity = ((String) props.getOrDefault("Rarity", "COMMON")).toUpperCase();
                allLootEntries.put(itemId, new LootEntry(item, amount, xp, rarity));
            }

            //Load shake drops
            if (root.containsKey("Shake_Drops")) {
                Map<String, List<Map<String, Object>>> shakeSection = (Map<String, List<Map<String, Object>>>) root.get("Shake_Drops");
                for (Map.Entry<String, List<Map<String, Object>>> entry : shakeSection.entrySet()) {
                    String entityId = entry.getKey();
                    List<Map<String, Object>> dropList = entry.getValue();
                    List<ShakeDrop> drops = new ArrayList<>();
                    for (Map<String, Object> drop : dropList) {
                        Identifier itemId = Identifier.tryParse((String) drop.get("Item"));
                        if (itemId == null || !Registries.ITEM.containsId(itemId)) continue;
                        int min = ((Number) drop.getOrDefault("Min", 1)).intValue();
                        int max = ((Number) drop.getOrDefault("Max", 1)).intValue();
                        double chance = ((Number) drop.getOrDefault("Chance", 100)).doubleValue();
                        drops.add(new ShakeDrop(itemId, min, max, chance));
                    }
                    shakeDropTables.put(entityId, drops);
                }
            }


            // Load DropRates
            Map<String, Object> dropRatesSection = (Map<String, Object>) root.get("DropRates");
            for (String tier : dropRatesSection.keySet()) {
                Map<String, Object> rateSection = (Map<String, Object>) dropRatesSection.get(tier);
                Map<String, Double> convertedRates = new HashMap<>();
                for (Map.Entry<String, Object> entry : rateSection.entrySet()) {
                    convertedRates.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                }
                if (tier.equals("Non_Treasure")) {
                    nonTreasureDropChances.putAll(convertedRates);
                } else {
                    tierDropRates.put(tier, convertedRates);
                }
            }

            // Load Enchantment Rarity Table
            enchantmentRarityTable.clear();
            Map<String, Map<String, Object>> enchantRaritySection = (Map<String, Map<String, Object>>) root.get("Enchantment_Rarity");
            if (enchantRaritySection != null) {
                for (Map.Entry<String, Map<String, Object>> rarityEntry : enchantRaritySection.entrySet()) {
                    String rarity = rarityEntry.getKey().toUpperCase();
                    Map<Identifier, Integer> enchantments = new HashMap<>();
                    for (Map.Entry<String, Object> ench : rarityEntry.getValue().entrySet()) {
                        Identifier id = Identifier.tryParse(ench.getKey());
                        if (id != null) {
                            enchantments.put(id, ((Number) ench.getValue()).intValue());
                        }
                    }
                    enchantmentRarityTable.put(rarity, enchantments);
                }
            }

            // Load Enchantment Drop Rates per Magic Find Tier
            enchantmentDropRatesByTier.clear();
            Map<String, Object> enchDropRates = (Map<String, Object>) root.get("Enchantment_Drop_Rates");
            if (enchDropRates != null) {
                for (String tier : enchDropRates.keySet()) {
                    Map<String, Object> rates = (Map<String, Object>) enchDropRates.get(tier);
                    Map<String, Double> converted = new HashMap<>();
                    for (Map.Entry<String, Object> entry : rates.entrySet()) {
                        converted.put(entry.getKey().toUpperCase(), ((Number) entry.getValue()).doubleValue());
                    }
                    enchantmentDropRatesByTier.put(tier, converted);
                }
            }

            //Load XP bonuses for enchantments.
            Map<String, Object> enchantXPConfig = (Map<String, Object>) root.get("Magic_Find_EnchantXP");
            if (enchantXPConfig != null) {
                for (Map.Entry<String, Object> entry : enchantXPConfig.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("XPPerEnchant")) {
                        xpPerEnchant = (Boolean) entry.getValue();
                    } else {
                        enchantXpByRarity.put(entry.getKey().toUpperCase(), ((Number) entry.getValue()).intValue());
                    }
                }
            }

            ServerReference.logConsole("Fishing loot config loaded successfully.");
            System.out.println("[FishingConfig] Loaded " + enchantmentRarityTable.size() + " enchantment rarity categories.");
            System.out.println("[FishingConfig] Loaded " + enchantmentDropRatesByTier.size() + " Magic Find drop rate tiers.");
        } catch (Exception e) {
            ServerReference.logConsole("Error loading fishing loot config:");
            e.printStackTrace();
        }
    }

    public static void saveDefault() {
        String defaultYaml = """
        Items:
          minecraft:cod:
            Amount: 1
            XP: 2
            Rarity: NON_TREASURE
          minecraft:salmon:
            Amount: 1
            XP: 3
            Rarity: NON_TREASURE
          minecraft:pufferfish:
            Amount: 1
            XP: 5
            Rarity: NON_TREASURE
          minecraft:tropical_fish:
            Amount: 1
            XP: 5
            Rarity: NON_TREASURE
          minecraft:iron_ingot:
            Amount: 1
            XP: 25
            Rarity: COMMON
          minecraft:gold_ingot:
            Amount: 1
            XP: 40
            Rarity: UNCOMMON
          minecraft:emerald:
            Amount: 1
            XP: 50
            Rarity: RARE
          minecraft:diamond:
            Amount: 1
            XP: 100
            Rarity: EPIC
          minecraft:netherite_ingot:
            Amount: 1
            XP: 200
            Rarity: MYTHIC
          minecraft:diamond_helmet:
            Amount: 1
            XP: 200
            Rarity: MYTHIC
          minecraft:diamond_sword:
            Amount: 1
            XP: 200
            Rarity: MYTHIC
          minecraft:diamond_pickaxe:
            Amount: 1
            XP: 200
            Rarity: MYTHIC
        
        DropRates:
          Tier_1:
            COMMON: 100
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 0
          Tier_2:
            COMMON: 0
            UNCOMMON: 100
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 0
          Tier_3:
            COMMON: 0
            UNCOMMON: 0
            RARE: 100
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 0
          Tier_4:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 100
            LEGENDARY: 0
            MYTHIC: 0
          Tier_5:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 100
            MYTHIC: 0
          Tier_6:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 100
          Tier_7:
            COMMON: 7.5
            UNCOMMON: 1.25
            RARE: 0.25
            EPIC: 0.1
            LEGENDARY: 0.01
            MYTHIC: 0.01
          Tier_8:
            COMMON: 7.5
            UNCOMMON: 1.25
            RARE: 0.25
            EPIC: 0.1
            LEGENDARY: 0.01
            MYTHIC: 0.01
          Tier_9:
            COMMON: 7.5
            UNCOMMON: 1.25
            RARE: 0.25
            EPIC: 0.1
            LEGENDARY: 0.01
            MYTHIC: 0.01
          Tier_10:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 1
          Non_Treasure:
            minecraft:cod: 70
            minecraft:salmon: 20
            minecraft:pufferfish: 8
            minecraft:tropical_fish: 2
        
        Enchantment_Rarity:
          COMMON:
            minecraft:efficiency: 1
            minecraft:unbreaking: 1
            minecraft:sharpness: 1
            minecraft:protection: 1
          UNCOMMON:
            minecraft:unbreaking: 2
            minecraft:knockback: 1
            minecraft:looting: 1
            minecraft:protection: 1
            minecraft:fire_protection: 1
            minecraft:projectile_protection: 1
            minecraft:blast_protection: 1
            minecraft:sharpness: 1
            minecraft:smite: 1
            minecraft:bane_of_arthropods: 1
          RARE:
            minecraft:fire_aspect: 1
            minecraft:unbreaking: 3
            minecraft:fortune: 2
            minecraft:protection: 2
            minecraft:fire_protection: 2
            minecraft:projectile_protection: 2
            minecraft:blast_protection: 2
            minecraft:sharpness: 2
            minecraft:smite: 2
            minecraft:bane_of_arthropods: 2
          EPIC:
            minecraft:power: 4
            minecraft:unbreaking: 4
            minecraft:flame: 1
            minecraft:protection: 3
            minecraft:fire_protection: 3
            minecraft:projectile_protection: 3
            minecraft:blast_protection: 3
            minecraft:sharpness: 3
            minecraft:smite: 3
            minecraft:bane_of_arthropods: 3
          LEGENDARY:
            minecraft:silk_touch: 1
            minecraft:unbreaking: 5
            minecraft:infinity: 1
            minecraft:protection: 4
            minecraft:fire_protection: 4
            minecraft:projectile_protection: 4
            minecraft:blast_protection: 4
            minecraft:sharpness: 4
            minecraft:smite: 4
            minecraft:bane_of_arthropods: 4
          MYTHIC:
            minecraft:mending: 1
            minecraft:unbreaking: 6
            minecraft:protection: 5
            minecraft:fire_protection: 5
            minecraft:projectile_protection: 5
            minecraft:blast_protection: 5
            minecraft:sharpness: 5
            minecraft:smite: 5
            minecraft:bane_of_arthropods: 5
          CURSES:
            minecraft:binding_curse: 1
            minecraft:vanishing_curse: 1
        
        # The chance of enchantments of each rarity, and chance to add a curse.
        # ExtraEnchantChance is the chance that it will add one more enchantment to the item.
        # (Stops adding enchantments when the probability fails. If its 0.5, think of it as consecutive coin flips and
        #  it will add as many enchantments as you get heads in a row)
        Enchantment_Drop_Rates:
          Tier_1:
            COMMON: 100
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 0
            CURSES: 50
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_2:
            COMMON: 0
            UNCOMMON: 100
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 0
            CURSES: 50
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_3:
            COMMON: 0
            UNCOMMON: 0
            RARE: 100
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 0
            CURSES: 50
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_4:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 100
            LEGENDARY: 0
            MYTHIC: 0
            CURSES: 50
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_5:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 100
            MYTHIC: 0
            CURSES: 50
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_6:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 100
            CURSES: 50
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_7:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0
            LEGENDARY: 0
            MYTHIC: 1
            CURSES: 1
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_8:
            COMMON: 0
            UNCOMMON: 0
            RARE: 0
            EPIC: 0.5
            LEGENDARY: 0.5
            MYTHIC: 0
            CURSES: 0.5
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_9:
            COMMON: 6.5
            UNCOMMON: 11.0
            RARE: 6.0
            EPIC: 2.0
            LEGENDARY: 1.0
            MYTHIC: 1.0
            CURSES: 0.15
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
          Tier_10:
            COMMON: 5.0
            UNCOMMON: 12.5
            RARE: 7.0
            EPIC: 2.5
            LEGENDARY: 1.25
            MYTHIC: 1.25
            CURSES: 0.2
            ExtraEnchantChance: 0.5
            ExtraCurseChance: 0.5
        
        Magic_Find_EnchantXP:
          COMMON: 10
          UNCOMMON: 15
          RARE: 20
          EPIC: 30
          LEGENDARY: 40
          MYTHIC: 50
          CURSES: 25
          XPPerEnchant: true
        
        Shake_Drops:
          minecraft:zombie:
            - Item: minecraft:rotten_flesh
              Min: 1
              Max: 3
              Chance: 100
          minecraft:creeper:
            - Item: minecraft:gunpowder
              Min: 1
              Max: 2
              Chance: 50
          minecraft:player:
            - Item: minecraft:player_head
              Min: 1
              Max: 1
              Chance: 20
        """;

        try {
            if (!CONFIG_DIR.toFile().exists()) CONFIG_DIR.toFile().mkdirs();
            if (!DEFAULT_FILE.getParentFile().exists()) DEFAULT_FILE.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write(defaultYaml);
                //ServerReference.logConsole("[FishingConfig] Fishing loot config created at: " + CONFIG_FILE);
            }

            try (FileWriter writer = new FileWriter(DEFAULT_FILE)) {
                writer.write(defaultYaml);
                //ServerReference.logConsole("[FishingConfig] Default fishing loot config created at: " + DEFAULT_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class LootEntry {
        public final Item item;
        public final int amount;
        public final int xp;
        public final String rarity;

        public LootEntry(Item item, int amount, int xp, String rarity) {
            this.item = item;
            this.amount = amount;
            this.xp = xp;
            this.rarity = rarity;
        }
    }

    public static class FishingSpotData {
        public BlockPos bobberPos;
        public int count;

        public FishingSpotData(BlockPos pos, int count) {
            this.bobberPos = pos;
            this.count = count;
        }
    }

    public static class ShakeDrop {
        public final Identifier itemId;
        public final int min;
        public final int max;
        public final double chance;

        public ShakeDrop(Identifier itemId, int min, int max, double chance) {
            this.itemId = itemId;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }
}


