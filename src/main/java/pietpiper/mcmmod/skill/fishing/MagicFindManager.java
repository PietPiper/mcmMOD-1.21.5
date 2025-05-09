package pietpiper.mcmmod.skill.fishing;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.XPUtil;

import java.util.*;

import static net.minecraft.registry.RegistryKeys.ENCHANTMENT;

public class MagicFindManager {
    private static final Random rand = new Random();

    public static void tryApplyMagicFind(ItemStack stack, ServerPlayerEntity player) {
        if (!isEnchantable(stack)) return;

        int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
        String tier = SkillConfigManager.getMagicHunterTier(level);
        if (tier == null) return;

        Map<String, Double> rarityWeights = FishingLootManager.getEnchantmentRarityWeightsForTier(tier);
        if (rarityWeights == null || rarityWeights.isEmpty()) return;

        Registry<Enchantment> enchantRegistry = player.getServer().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        int enchantCount = rollEnchantCount(tier);
        Set<Identifier> appliedEnchantments = new HashSet<>();

        int applied = 0;
        while (applied < enchantCount) {
            String rolledRarity;
            do {
                rolledRarity = rollRarityWeighted(rarityWeights);
            } while ("CURSES".equalsIgnoreCase(rolledRarity));

            Map<Identifier, Integer> pool = FishingLootManager.getEnchantmentsForRarity(rolledRarity);
            if (pool == null || pool.isEmpty()) break;

            List<Map.Entry<Identifier, Integer>> choices = pool.entrySet().stream()
                    .filter(e -> !appliedEnchantments.contains(e.getKey()))
                    .filter(e -> {
                        RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, e.getKey());
                        RegistryEntry.Reference<Enchantment> ench = enchantRegistry.getOrThrow(key);
                        Enchantment enchObj = ench.value();
                        return ench != null && enchObj.isAcceptableItem(stack);
                    })
                    .toList();

            if (choices.isEmpty()) break;

            Map.Entry<Identifier, Integer> selected = choices.get(rand.nextInt(choices.size()));
            RegistryKey<Enchantment> slctedKey = RegistryKey.of(RegistryKeys.ENCHANTMENT, selected.getKey());
            RegistryEntry.Reference<Enchantment> slctedEnch = enchantRegistry.getOrThrow(slctedKey);

            stack.addEnchantment(slctedEnch, selected.getValue());
            appliedEnchantments.add(selected.getKey());
            applied++;

            XPUtil.addXP(player.getUuid(), Skill.FISHING,
                    FishingLootManager.getXpForEnchantmentRarity(rolledRarity, selected.getValue()));
        }

        // Apply curses
        double curseChance = rarityWeights.getOrDefault("CURSES", 0.0);
        if (rand.nextDouble() < (curseChance / 100.0)) {
            Map<Identifier, Integer> cursePool = FishingLootManager.getEnchantmentsForRarity("CURSES");
            if (cursePool != null && !cursePool.isEmpty()) {
                int curseCount = rollCurseCount(tier);
                for (int i = 0; i < curseCount; i++) {
                    List<Map.Entry<Identifier, Integer>> curses = cursePool.entrySet().stream()
                            .filter(e -> !appliedEnchantments.contains(e.getKey()))
                            .filter(e -> {
                                RegistryKey<Enchantment> curseKey = RegistryKey.of(RegistryKeys.ENCHANTMENT, e.getKey());
                                RegistryEntry.Reference<Enchantment> curseEntry = enchantRegistry.getOrThrow(curseKey);
                                Enchantment curse = curseEntry.value();
                                return curse != null && curse.isAcceptableItem(stack);
                            })
                            .toList();

                    if (curses.isEmpty()) break;

                    Map.Entry<Identifier, Integer> curseEntry = curses.get(rand.nextInt(curses.size()));
                    RegistryKey<Enchantment> curseKey = RegistryKey.of(RegistryKeys.ENCHANTMENT, curseEntry.getKey());
                    RegistryEntry.Reference<Enchantment> curseRegEntry = enchantRegistry.getOrThrow(curseKey);

                    stack.addEnchantment(curseRegEntry, curseEntry.getValue());
                    appliedEnchantments.add(curseEntry.getKey());

                    XPUtil.addXP(player.getUuid(), Skill.FISHING,
                            FishingLootManager.getXpForEnchantmentRarity("CURSES", curseEntry.getValue()));
                }
            }
        }
    }

    private static boolean isEnchantable(ItemStack stack) {
        return stack.isOf(Items.BOOK) || stack.isDamageable();
    }

    private static int rollEnchantCount(String tier) {
        double continueChance = FishingLootManager.getContinueChanceForTier(tier);
        int count = 1;
        while (rand.nextDouble() < continueChance) {
            count++;
        }
        return count;
    }

    private static int rollCurseCount(String tier) {
        return rand.nextDouble() < 0.1 ? 1 : 0;
    }

    private static String rollRarityWeighted(Map<String, Double> weights) {
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = rand.nextDouble() * total;
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return "COMMON"; // fallback
    }
}

