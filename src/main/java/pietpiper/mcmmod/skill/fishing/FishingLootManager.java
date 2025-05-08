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

            ServerReference.logConsole("Fishing loot config loaded successfully.");
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

DropRates:
  Tier_1:
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
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
    COMMON: 7.5
    UNCOMMON: 1.25
    RARE: 0.25
    EPIC: 0.1
    LEGENDARY: 0.01
    MYTHIC: 0.01
  Non_Treasure:
    minecraft:cod: 70
    minecraft:salmon: 20
    minecraft:pufferfish: 8
    minecraft:tropical_fish: 2
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
