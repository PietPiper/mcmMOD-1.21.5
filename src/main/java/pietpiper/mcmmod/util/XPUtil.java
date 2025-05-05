package pietpiper.mcmmod.util;

import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;

import java.util.UUID;

/**
 * XPUtil handles XP curve math and skill level-up logic for players.
 * It defines the custom XP progression and methods for modifying player stats.
 */
public class XPUtil {

    /**
     * Returns the XP required to go from a given level to the next level.
     *
     * @param level the current level
     * @return XP needed to level up
     */
    public static int getXPRequiredForLevelUp(int level) {
        if (level <= 2000) {
            return 20 * level + 1020;
        } else {
            return 5 * level + 31020;
        }
    }

    /**
     * Adds XP to a given skill for a player and handles level-up logic.
     * ToDO: Add level up listening functionality
     * @param uuid  the player's UUID
     * @param skill the skill to modify
     * @param amount the amount of XP to add
     */
    public static void addXP(UUID uuid, Skill skill, int amount) {
        int xp = PlayerDataManager.getXP(uuid, skill);
        int remaining = PlayerDataManager.getRemainingXP(uuid, skill);
        int level = PlayerDataManager.getLevel(uuid, skill);

        xp += amount;
        remaining -= amount;

        // Handle level-ups, including multiple in one burst
        while (remaining <= 0) {
            //Xp being "taken" for current level up.
            int xpForLevelUp = getXPRequiredForLevelUp(level);
            //Increase level.
            level++;
            //Subtract xp "spent" to level up.
            xp -= xpForLevelUp;
            //Update remaining xp to what is required for the next level up minus leftover xp.
            remaining = getXPRequiredForLevelUp(level) - xp;
        }

        // Update all values in the database
        PlayerDataManager.updatePlayerExperience(uuid, skill, xp, remaining, level);

        // Optionally, trigger future event hook here:
        // SkillEvents.LEVEL_UP.invoker().onLevelUp(player, skill, level);
    }
    /**
     * Calculates the total XP required to reach a given level from level 0.
     *
     * Uses a piecewise function:
     * - For levels 0 to 2000: 10x(101 + x)
     * - For levels > 2000: continues with a quadratic and linear tail.
     *
     * @param level The level to reach
     * @return The total XP required from level 0 to get to the specified level
     */
    public static int getTotalXPToReachLevel(int level) {
        if (level <= 2000) {
            // For level <= 2000, use the standard XP curve: 10x(101 + x)
            return 10 * level * (101 + level);
        } else {
            // Precomputed XP required to reach level 2000
            int f2000 = 10 * 2000 * (101 + 2000);

            // Quadratic XP scaling after level 2000 (carryover curve)
            // Derived from: 5 * ((x - 1)x - (2000 * 1999)) / 2
            int quad = (((level - 1) * level) - (2000 * 1999)) / 2;

            // Linear component for the tail of the XP curve
            int linear = 31020 * (level - 2000);

            // Final lifetime XP required for level > 2000
            return f2000 + 5 * quad + linear;
        }
    }

    /**
     * Returns the total lifetime XP a player has gained in a given skill.
     *
     * This includes:
     * - All XP required to reach their current level
     * - Their XP progress within that level
     *
     * @param uuid  The player's UUID
     * @param skill The skill to calculate for
     * @return Lifetime XP earned for this skill
     */
    public static int getLifetimeXP(UUID uuid, Skill skill) {
        int level = PlayerDataManager.getLevel(uuid, skill);
        int currentXP = PlayerDataManager.getXP(uuid, skill);

        // Sum of XP to reach current level + current level progress
        return getTotalXPToReachLevel(level) + currentXP;
    }
}
