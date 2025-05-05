package pietpiper.mcmmod.skill;

import pietpiper.mcmmod.config.ConfigManager;

/**
 * The Skill enum defines all skill types used in Mcmmod.
 *
 * Each enum constant represents a skill (e.g., FISHING, MINING), and stores metadata
 * such as a display name, text color for UI, and a human-readable description on how to level it (for now).
 *
 * This enum is used throughout the mod for:
 * - Displaying XP bars and skill names
 * - Initializing and accessing the correct database columns for each skill
 * - Typing skill parameters and avoiding spelling mistakes or referencing non-existent skills
 *
 * To add a new skill, define it here
 */
public enum Skill {
    FISHING("Fishing", "Cast lines and catch fish."),         // Aqua
    TAMING("Taming", "Tame and use passive mobs."),           // Gold
    MINING("Mining", "Break naturally generated blocks with a pick."), // Gray
    ACROBATICS("Acrobatics", "Take fall damage and roll."),   // White
    WOODCUTTING("Woodcutting", "Chop logs."),                 // Dark Green
    HERBALISM("Herbalism", "Harvest crops and plants."),      // Green
    EXCAVATION("Excavation", "Dig naturally generated blocks with a shovel."), // Yellow
    UNARMED("Unarmed", "Fight without weapons."),             // Red
    ARCHERY("Archery", "Shoot stuff."),                       // Dark Purple
    SWORDS("Swords", "Slash enemies with swords."),           // Dark Red
    AXES("Axes", "Chop and cleave with axes."),              // Dark Aqua
    ALCHEMY("Alchemy", "Brew potions."),                      // Light Purple
    SMELTING("Smelting", "Smelt stuff."),                     // Dark Blue
    ENCHANTING("Enchanting", "Empower items with enchantments or remove them."), // Blue
    GLIDING("Gliding", "Fly through the sky with elytra!");   // Black

    // The human-friendly name used in UI and commands
    private final String displayName;


    // Short description of the skill shown in help menus, hover text, etc.
    private final String description;

    /**
     * Private constructor for Skill enum constants.
     * Called automatically by the enum declarations at the top.
     */
    Skill(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /** Returns the skill's display name (e.g. "Fishing") */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns a short description of the skill */
    public String getDescription() {
        return description;
    }

    /**
     * Looks up a Skill enum constant by its display name.
     * This is used for parsing user input (e.g. "/getxp Fishing")
     *
     * @param input The input string, case-insensitive (e.g. "Fishing" or "fishing")
     * @return The matching Skill, or null if not found
     */
    public static Skill fromName(String input) {
        for (Skill skill : values()) {
            if (skill.displayName.equalsIgnoreCase(input)) {
                return skill;
            }
        }
        return null;
    }
}