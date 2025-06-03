package pietpiper.mcmmod.skill.fishing;

import com.jcraft.jorbis.Block;
import net.fabricmc.fabric.api.item.v1.FabricComponentMapBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.*;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.jetbrains.annotations.Nullable;
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
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ItemEntity;
import com.mojang.authlib.GameProfile;

import net.minecraft.component.ComponentsAccess;

public class FishingLootManager {
    //private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().resolve("fishing_treasure_config.yml").toString());

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("MCMMOD");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("fishing_treasure_config.yml").toFile();
    private static final File DEFAULT_FILE = CONFIG_DIR.resolve("defaults/fishing_treasure_config_defaults.yml").toFile();

    private static final Map<String, LootEntry> allLootEntries = new HashMap<>();
    private static final TreeMap<String, Map<String, Double>> tierDropRates = new TreeMap<>();
    private static final Map<String, Double> nonTreasureDropChances = new HashMap<>();
    private static final Map<String, Map<Identifier, Integer>> enchantmentRarityTable = new HashMap<>();
    private static final TreeMap<String, Map<String, Double>> enchantmentDropRatesByTier = new TreeMap<>();
    private static final Map<String, Integer> enchantXpByRarity = new HashMap<>();
    private static boolean xpPerEnchant = false;
    private static final Map<UUID, FishingSpotData> fishingSpotMap = new ConcurrentHashMap<>();
    private static final Map<String, List<ShakeDrop>> shakeDropTables = new HashMap<>();

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
                //ServerReference.logConsole("Default fishing loot config created at: " + CONFIG_FILE);
            }

            try (FileWriter writer = new FileWriter(DEFAULT_FILE)) {
                writer.write(defaultYaml);
                //ServerReference.logConsole("Default fishing loot config created at: " + DEFAULT_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleShakeDrops(LivingEntity target, ServerPlayerEntity player) {
        //player.sendMessage(Text.literal("§aYou just shook that poor motherfucker"), false);
        String id = Registries.ENTITY_TYPE.getId(target.getType()).toString();
        if (target instanceof ServerPlayerEntity) {
            id = "minecraft:player";  // Use the key for player drops
        }

        List<ShakeDrop> drops = shakeDropTables.getOrDefault(id, List.of());
        if (drops.isEmpty()){
            player.sendMessage(Text.literal("§aNo shake drops for that mob."), false);
            return;
        }

        double roll = player.getRandom().nextDouble() * 100;
        ShakeDrop selected = null;
        for (ShakeDrop drop : drops) {
            if (roll < drop.chance) {
                selected = drop;
                break;
            }
        }

        if (selected == null) {
            player.sendMessage(Text.literal("§aYour shake drop was null :("), false);
            return;
        }

        // Apply damage to the entity
        float damage = Math.min((float)(target.getMaxHealth() * SkillConfigManager.getShakeDamagePercent()), SkillConfigManager.getMaxShakeDamage());
        DamageSource source = target.getDamageSources().generic();
        ServerWorld world = (ServerWorld) target.getWorld();
        target.damage(world, source, damage);

        ItemStack stack;
        if (selected.itemId.equals(Items.PLAYER_HEAD.getRegistryEntry().registryKey().getValue()) && target instanceof ServerPlayerEntity targetPlayer) {
            //Code im memorium for what I worked towards for hours and was actually pretty close just needed to be profileComponent.
            /*stack = new ItemStack(Items.PLAYER_HEAD, 1);
            Identifier compId = Identifier.tryParse("minecraft:profile");
            ComponentType<GameProfile> skullOwnerType = (ComponentType<GameProfile>) Registries.DATA_COMPONENT_TYPE.get(compId);
            stack.set(skullOwnerType, targetPlayer.getGameProfile());*/
            stack = new ItemStack(Items.PLAYER_HEAD, 1);
            GameProfile profile = player.getGameProfile();
            ProfileComponent profileComponent = new ProfileComponent(profile);
            stack.set(DataComponentTypes.PROFILE, profileComponent);
        } else {
            Item item = Registries.ITEM.get(selected.itemId);
            int count = selected.min + (selected.max > selected.min ? player.getRandom().nextInt(selected.max - selected.min + 1) : 0);
            stack = new ItemStack(item, count);

        }

        //ItemStack stack = new ItemStack(item, count);
        // Spawn position
        Vec3d spawnPos = target.getPos();
        // Create and configure the item entity
        ItemEntity entity = new ItemEntity(player.getWorld(), spawnPos.x, spawnPos.y + 0.5, spawnPos.z, stack);
        // Pull velocity toward player
        Vec3d velocity = player.getPos().add(0, 1.5, 0).subtract(spawnPos).normalize().multiply(0.3);entity.setVelocity(velocity);
        // Spawn the item
        player.getWorld().spawnEntity(entity);
        //player.sendMessage(Text.literal("§aLoot should have been dropped."), false);
    }

    public static ItemStack getLootForPlayer(ServerPlayerEntity player, FishingBobberEntity bobber, Vec3d lastCast) {
        UUID playerId = player.getUuid();
        FishingSpotData data = fishingSpotMap.get(playerId);
        int distFromSpot = SkillConfigManager.getMinFishingSpotDistance();
        //int distFromSpot = 3;
        int distSquared = distFromSpot * distFromSpot;
        //int distSquared = 3 * 3;
        int maxFish = SkillConfigManager.getMaxFishPerSpot();
        //int maxFish = 9;

        BlockPos bobberPos = bobber.getBlockPos();

        if (data == null) {
            fishingSpotMap.put(playerId, new FishingSpotData(bobberPos, 1));
        } else {
            if (data.bobberPos.getSquaredDistance(bobberPos) <= distSquared) {
                data.count++;
            } else {
                data.bobberPos = bobberPos;
                data.count = 1;
            }

            if (data.count == maxFish) {
                player.sendMessage(Text.of("§eThis fishing spot is starting to dry up... try a new one soon."), false);
            } else if (data.count > maxFish) {
                player.sendMessage(Text.of("§cThis spot seems empty. Try fishing somewhere else."), false);
                return ItemStack.EMPTY;
            }
        }

        int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
        String tier = SkillConfigManager.getFishingTier(level);

        if (tier == null || !tierDropRates.containsKey(tier)) {
            return getNonTreasureLoot(player);
        }

        Map<String, Double> tierRates = tierDropRates.getOrDefault(tier, Map.of());
        double totalTreasureChance = tierRates.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = new Random().nextDouble();

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
                        spawnTreasureParticles((ServerWorld) player.getWorld(), lastCast, entry.getKey(), false);
                        return new ItemStack(drop.item, drop.amount);
                    }
                }
            }
        }
        return getNonTreasureLoot(player);
    }

    public static void spawnTreasureParticles(ServerWorld world, Vec3d pos, String rarity, boolean enchanted) {
        ServerReference.logConsole("[Fishing Particles] Particles spawned. Enchanted: " + enchanted);
        int color = 0;
        if(rarity != null) {
            color = getRarityColor(rarity);
        }
        float scale = 0.40f;  // Particle size (can adjust)
        DustParticleEffect dust = new DustParticleEffect(color, scale);

        // When just spawning the enchanted particles after the fact.
        if (enchanted) {
            for (int i = 0; i < 10; i++) {
                world.spawnParticles(ParticleTypes.ENCHANT, pos.getX(), pos.getY(), pos.getZ(), 2, .5, .5, .5, .5);
            }
        }
        else {
            // Spawn multiple particles for effect
            for (int i = 0; i < 10; i++) {
                world.spawnParticles(dust, pos.getX(), pos.getY(), pos.getZ(), 10, .5, .5, .5, .5);
            }
        }
    }

    public static int getRarityColor(String rarity) {
        return switch (rarity.toUpperCase()) {
            case "COMMON" -> 0x808080;      // Gray
            case "UNCOMMON" -> 0x15B01C;    // Green
            case "RARE" -> 0x0ECCEA;        // Blue
            case "EPIC" -> 0xAA00FF;        // Purple
            case "LEGENDARY" -> 0xFFAA00;   // Orange
            case "MYTHIC" -> 0xDF0909;     // Red
            default -> 0xFFFFFF;            // White fallback
        };
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

    public static double getContinueChanceForTier(String tier, boolean curse) {
        if(!curse) {
            Map<String, Double> tierMap = enchantmentDropRatesByTier.getOrDefault(tier, Map.of());
            if (tierMap == null) return 0.0;
            return tierMap.getOrDefault("EXTRAENCHANTCHANCE", 0.0);
        }
        else {
            Map<String, Double> tierMap = enchantmentDropRatesByTier.getOrDefault(tier, Map.of());
            if (tierMap == null) return 0.0;
            return tierMap.getOrDefault("EXTRACURSECHANCE", 0.0);
        }
    }

    public static int getFishAtSpot(ServerPlayerEntity player) {
        FishingSpotData spotFished = fishingSpotMap.get(player.getUuid());
        return (spotFished != null) ? spotFished.count : 0;
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

    private static class FishingSpotData {
        BlockPos bobberPos;
        int count;

        FishingSpotData(BlockPos pos, int count) {
            this.bobberPos = pos;
            this.count = count;
        }
    }

    private static class ShakeDrop {
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
