package pietpiper.mcmmod.skill;

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
    FISHING("Fishing", 0x00FFFF, "Cast lines and catch fish."),         // Aqua
    TAMING("Taming", 0xFFAA00, "Tame and use passive mobs."),           // Gold
    MINING("Mining", 0xAAAAAA, "Break naturally generated blocks with a pick."), // Gray
    ACROBATICS("Acrobatics", 0xFFFFFF, "Take fall damage and roll."),   // White
    WOODCUTTING("Woodcutting", 0x00AA00, "Chop logs."),                 // Dark Green
    HERBALISM("Herbalism", 0x55FF55, "Harvest crops and plants."),      // Green
    EXCAVATION("Excavation", 0xFFFF55, "Dig naturally generated blocks with a shovel."), // Yellow
    UNARMED("Unarmed", 0xFF5555, "Fight without weapons."),             // Red
    ARCHERY("Archery", 0xAA00AA, "Shoot stuff."),                       // Dark Purple
    SWORDS("Swords", 0xAA0000, "Slash enemies with swords."),           // Dark Red
    AXES("Axes", 0x00AAAA, "Chop and cleave with axes."),              // Dark Aqua
    ALCHEMY("Alchemy", 0xFF55FF, "Brew potions."),                      // Light Purple
    SALVAGE("Salvage", 0x555555, "Recover enchantments from gear [If you know enough about them :)]."), // Dark Gray
    SMELTING("Smelting", 0x0000AA, "Smelt stuff."),                     // Dark Blue
    ENCHANTING("Enchanting", 0x5555FF, "Empower items with enchantments or remove them."), // Blue
    GLIDING("Gliding", 0x000000, "Fly through the sky with elytra!");   // Black

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