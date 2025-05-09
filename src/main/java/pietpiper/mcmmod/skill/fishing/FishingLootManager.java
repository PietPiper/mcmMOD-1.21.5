package pietpiper.mcmmod.skill.fishing;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.Yaml;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.ServerReference;
import pietpiper.mcmmod.util.XPUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;

public class FishingLootManager {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("fishing_treasure_config.yml").toString());

    private static final Map<String, LootEntry> allLootEntries = new HashMap<>();
    private static final TreeMap<String, Map<String, Double>> tierDropRates = new TreeMap<>();
    private static final Map<String, Double> nonTreasureDropChances = new HashMap<>();
    private static final Map<String, Map<Identifier, Integer>> enchantmentRarityTable = new HashMap<>();
    private static final TreeMap<String, Map<String, Double>> enchantmentDropRatesByTier = new TreeMap<>();
    private static final Map<String, Integer> enchantXpByRarity = new HashMap<>();
    private static boolean xpPerEnchant = false;

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
            System.out.println("[FishingLootManager] Loaded " + enchantmentRarityTable.size() + " enchantment rarity categories.");
            System.out.println("[FishingLootManager] Loaded " + enchantmentDropRatesByTier.size() + " Magic Find drop rate tiers.");
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
    COMMON: 0
    UNCOMMON: 0
    RARE: 0
    EPIC: 0
    LEGENDARY: 0
    MYTHIC: 1
  Tier_2:
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
  Tier_3:
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
  Tier_4:
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
  Tier_5:
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
  Tier_6:
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
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
    COMMON: 5.0
    UNCOMMON: 1.0
    RARE: 0.1
    EPIC: 0.01
    LEGENDARY: 0.01
    MYTHIC: 0.01
    CURSES: 0.01
    ExtraEnchantChance: 0.5
  Tier_2:
    COMMON: 7.5
    UNCOMMON: 1.0
    RARE: 0.1
    EPIC: 0.01
    LEGENDARY: 0.01
    MYTHIC: 0.01
    CURSES: 0.01
    ExtraEnchantChance: 0.5
  Tier_3:
    COMMON: 7.5
    UNCOMMON: 2.5
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
    CURSES: 0.02
    ExtraEnchantChance: 0.5
  Tier_4:
    COMMON: 10.0
    UNCOMMON: 2.75
    RARE: 0.5
    EPIC: 0.1
    LEGENDARY: 0.05
    MYTHIC: 0.05
    CURSES: 0.05
    ExtraEnchantChance: 0.5
  Tier_5:
    COMMON: 10.0
    UNCOMMON: 4.0
    RARE: 0.75
    EPIC: 0.25
    LEGENDARY: 0.1
    MYTHIC: 0.1
    CURSES: 0.05
    ExtraEnchantChance: 0.5
  Tier_6:
    COMMON: 9.5
    UNCOMMON: 5.5
    RARE: 1.75
    EPIC: 0.5
    LEGENDARY: 0.25
    MYTHIC: 0.25
    CURSES: 0.05
    ExtraEnchantChance: 0.5
  Tier_7:
    COMMON: 0
    UNCOMMON: 0
    RARE: 0
    EPIC: 0
    LEGENDARY: 0
    MYTHIC: 1
    CURSES: 1
    ExtraEnchantChance: 0.75
  Tier_8:
    COMMON: 0
    UNCOMMON: 0
    RARE: 0
    EPIC: 0.5
    LEGENDARY: 0.5
    MYTHIC: 0
    CURSES: 0.5
    ExtraEnchantChance: 0.5
  Tier_9:
    COMMON: 6.5
    UNCOMMON: 11.0
    RARE: 6.0
    EPIC: 2.0
    LEGENDARY: 1.0
    MYTHIC: 1.0
    CURSES: 0.15
    ExtraEnchantChance: 0.5
  Tier_10:
    COMMON: 5.0
    UNCOMMON: 12.5
    RARE: 7.0
    EPIC: 2.5
    LEGENDARY: 1.25
    MYTHIC: 1.25
    CURSES: 0.2
    ExtraEnchantChance: 0.5
    
Magic_Find_EnchantXP:
  COMMON: 10
  UNCOMMON: 15
  RARE: 20
  EPIC: 30
  LEGENDARY: 40
  MYTHIC: 50
  CURSES: 25
  XPPerEnchant: true
""";

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(defaultYaml);
            ServerReference.logConsole("Default fishing loot config created at: " + CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getLootForPlayer(ServerPlayerEntity player) {
        int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
        String tier = SkillConfigManager.getFishingTier(level);

        if (tier == null || !tierDropRates.containsKey(tier)) {
            return getNonTreasureLoot(player);
        }

        Map<String, Double> tierRates = tierDropRates.getOrDefault(tier, Map.of());
        double totalTreasureChance = tierRates.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = new Random().nextDouble();

        System.out.println("[FishingLootManager] Level: " + level);
        System.out.println("[FishingLootManager] Tier: " + tier);
        System.out.println("[FishingLootManager] Tier rates: " + tierRates);
        System.out.println("[FishingLootManager] Total treasure chance: " + totalTreasureChance);

        if (roll < totalTreasureChance / 100.0) {
            double tierRoll = new Random().nextDouble() * totalTreasureChance;
            double cumulative = 0;
            for (Map.Entry<String, Double> entry : tierRates.entrySet()) {
                cumulative += entry.getValue();
                if (tierRoll < cumulative) {
                    List<LootEntry> matching = allLootEntries.entrySet().stream()
                            .filter(e -> e.getValue().rarity.equalsIgnoreCase(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .toList();
                    if (!matching.isEmpty()) {
                        LootEntry drop = matching.get(new Random().nextInt(matching.size()));
                        XPUtil.addXP(player.getUuid(), Skill.FISHING, drop.xp);
                        return new ItemStack(drop.item, drop.amount);
                    }
                }
            }
        }

        return getNonTreasureLoot(player);
    }

    private static ItemStack getNonTreasureLoot(ServerPlayerEntity player) {
        double fallbackRoll = new Random().nextDouble() * 100;
        double cumulative = 0;
        for (Map.Entry<String, Double> entry : nonTreasureDropChances.entrySet()) {
            cumulative += entry.getValue();
            if (fallbackRoll < cumulative) {
                LootEntry drop = allLootEntries.get(entry.getKey());
                if (drop != null) {
                    XPUtil.addXP(player.getUuid(), Skill.FISHING, drop.xp);
                    return new ItemStack(drop.item, drop.amount);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static int getXpForEnchantmentRarity(String rarity, int enchantCount) {
        int base = enchantXpByRarity.getOrDefault(rarity.toUpperCase(), 0);
        return xpPerEnchant ? base * enchantCount : 0;
    }

    public static Map<Identifier, Integer> getEnchantmentsForRarity(String rarity) {
        return enchantmentRarityTable.getOrDefault(rarity.toUpperCase(), Map.of());
    }

    public static Map<String, Double> getEnchantmentRarityWeightsForTier(String tier) {
        return enchantmentDropRatesByTier.getOrDefault(tier, Map.of());
    }

    public static double getContinueChanceForTier(String tier) {
        Map<String, Double> tierMap = enchantmentDropRatesByTier.getOrDefault(tier, Map.of());
        if (tierMap == null) return 0.0;
        return tierMap.getOrDefault("ExtraEnchantChance", 0.0);
    }

    private static class LootEntry {
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
}
