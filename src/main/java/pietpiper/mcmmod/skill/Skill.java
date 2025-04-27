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
 * - Typing skill parameters and avoiding spelling mistakes or referencing non-existant skills
 *
 * To add a new skill, define it here
 */
public enum Skill {
    FISHING("Fishing", ConfigManager.getConfig().defaultFishingColor, "Cast lines and catch fish."),         // Aqua
    TAMING("Taming", ConfigManager.getConfig().defaultTamingColor, "Tame and use passive mobs."),           // Gold
    MINING("Mining", ConfigManager.getConfig().defaultMiningColor, "Break naturally generated blocks with a pick."), // Gray
    ACROBATICS("Acrobatics", ConfigManager.getConfig().defaultAcrobaticsColor, "Take fall damage and roll."),   // White
    WOODCUTTING("Woodcutting", ConfigManager.getConfig().defaultWoodcuttingColor, "Chop logs."),                 // Dark Green
    HERBALISM("Herbalism", ConfigManager.getConfig().defaultHerbalismColor, "Harvest crops and plants."),      // Green
    EXCAVATION("Excavation", ConfigManager.getConfig().defaultExcavationColor, "Dig naturally generated blocks with a shovel."), // Yellow
    UNARMED("Unarmed", ConfigManager.getConfig().defaultUnarmedColor, "Fight without weapons."),             // Red
    ARCHERY("Archery", ConfigManager.getConfig().defaultArcheryColor, "Shoot stuff."),                       // Dark Purple
    SWORDS("Swords", ConfigManager.getConfig().defaultSwordsColor, "Slash enemies with swords."),           // Dark Red
    AXES("Axes", ConfigManager.getConfig().defaultAxesColor, "Chop and cleave with axes."),              // Dark Aqua
    ALCHEMY("Alchemy", ConfigManager.getConfig().defaultAlchemyColor, "Brew potions."),                      // Light Purple
    SMELTING("Smelting", ConfigManager.getConfig().defaultSmeltingColor, "Smelt stuff."),                     // Dark Blue
    ENCHANTING("Enchanting", ConfigManager.getConfig().defaultEnchantingColor, "Empower items with enchantments or remove them."), // Blue
    GLIDING("Gliding", ConfigManager.getConfig().defaultGlidingColor, "Fly through the sky with elytra!");   // Black

    // The human-friendly name used in UI and commands
    private final String displayName;

    // The Minecraft text color hex code used when rendering this skill in messages/bars
    private final int colorHex;

    // Short description of the skill shown in help menus, hover text, etc.
    private final String description;

    /**
     * Private constructor for Skill enum constants.
     * Called automatically by the enum declarations at the top.
     */
    Skill(String displayName, int color, String description) {
        this.displayName = displayName;
        this.colorHex = color;
        this.description = description;
    }

    /** Returns the skill's display name (e.g. "Fishing") */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the associated color for use in UI (e.g. scoreboards, bars, messages) */
    public int getColorHex() {
        return colorHex;
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