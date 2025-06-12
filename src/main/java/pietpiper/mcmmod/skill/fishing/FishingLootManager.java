package pietpiper.mcmmod.skill.fishing;

import static pietpiper.mcmmod.config.FishingConfig.*;
import pietpiper.mcmmod.config.FishingConfig;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.ServerReference;
import pietpiper.mcmmod.util.XPUtil;

import net.minecraft.component.*;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import com.mojang.authlib.GameProfile;

public class FishingLootManager {

    private static final Map<UUID, FishingConfig.FishingSpotData> fishingSpotMap = new ConcurrentHashMap<>();

    public static void handleShakeDrops(LivingEntity target, ServerPlayerEntity player) {
        //player.sendMessage(Text.literal("§aYou just shook that poor motherfucker"), false);
        String id = Registries.ENTITY_TYPE.getId(target.getType()).toString();
        if (target instanceof ServerPlayerEntity) {
            id = "minecraft:player";  // Use the key for player drops
        }

        List<FishingConfig.ShakeDrop> drops = shakeDropTables.getOrDefault(id, List.of());
        if (drops.isEmpty()){
            player.sendMessage(Text.literal("§aNo shake drops for that mob."), false);
            return;
        }

        double roll = player.getRandom().nextDouble() * 100;
        FishingConfig.ShakeDrop selected = null;
        for (FishingConfig.ShakeDrop drop : drops) {
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

        // Spawn position
        Vec3d spawnPos = target.getPos();
        // Create and configure the item entity
        ItemEntity entity = new ItemEntity(player.getWorld(), spawnPos.x, spawnPos.y + 0.5, spawnPos.z, stack);
        // Pull velocity toward player
        Vec3d velocity = player.getPos().add(0, 1.5, 0).subtract(spawnPos).normalize().multiply(0.3);entity.setVelocity(velocity);
        // Spawn the item
        player.getWorld().spawnEntity(entity);
        //player.sendMessage(Text.literal("§aLoot should have been dropped."), false);
        ServerReference.logConsole("[FishingLootManager] Shake lot should have been dropped.");
    }

    public static ItemStack getLootForPlayer(ServerPlayerEntity player, FishingBobberEntity bobber, Vec3d lastCast) {
        UUID playerId = player.getUuid();
        FishingConfig.FishingSpotData data = fishingSpotMap.get(playerId);
        int distFromSpot = SkillConfigManager.getMinFishingSpotDistance();
        int distSquared = distFromSpot * distFromSpot;
        int maxFish = SkillConfigManager.getMaxFishPerSpot();

        BlockPos bobberPos = bobber.getBlockPos();

        if (data == null) {
            fishingSpotMap.put(playerId, new FishingConfig.FishingSpotData(bobberPos, 1));
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
                    List<FishingConfig.LootEntry> matching = allLootEntries.entrySet().stream()
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
        FishingConfig.FishingSpotData spotFished = fishingSpotMap.get(player.getUuid());
        return (spotFished != null) ? spotFished.count : 0;
    }
}
