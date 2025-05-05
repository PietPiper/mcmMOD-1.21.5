package pietpiper.mcmmod.data;

import pietpiper.mcmmod.config.ConfigManager;
import pietpiper.mcmmod.skill.Skill;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * McmPlayer holds all skill-related data for a single player in memory,
 * allowing fast access without repeated database lookups.
 */
public class McmPlayer {
    private final UUID uuid;
    private final Map<Skill, Integer> xpMap;
    private final Map<Skill, Integer> remainingXpMap;
    private final Map<Skill, Integer> levelMap;
    private PlayerSettings settings = PlayerSettingsManager.ConfiguredPlayerSettings();

    public McmPlayer(UUID uuid) {
        this.uuid = uuid;
        this.xpMap = new EnumMap<>(Skill.class);
        this.remainingXpMap = new EnumMap<>(Skill.class);
        this.levelMap = new EnumMap<>(Skill.class);

        // Initialize with default values
        for (Skill skill : Skill.values()) {
            xpMap.put(skill, PlayerDataManager.STARTING_XP);
            remainingXpMap.put(skill, PlayerDataManager.STARTING_REMAINING_XP);
            levelMap.put(skill, ConfigManager.getConfig().startingLevel);
        }
    }

    public void updateSettings(PlayerSettings playerSettings) {
        settings = playerSettings;
    }

    public PlayerSettings getSettings() {
        return settings;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getXP(Skill skill) {
        return xpMap.getOrDefault(skill, PlayerDataManager.STARTING_XP);
    }

    public void setXP(Skill skill, int xp) {
        xpMap.put(skill, xp);
    }

    public int getRemainingXP(Skill skill) {
        return remainingXpMap.getOrDefault(skill, PlayerDataManager.STARTING_REMAINING_XP);
    }

    public void setRemainingXP(Skill skill, int remainingXp) {
        remainingXpMap.put(skill, remainingXp);
    }

    public int getLevel(Skill skill) {
        return levelMap.getOrDefault(skill, ConfigManager.getConfig().startingLevel);
    }

    public void setLevel(Skill skill, int level) {
        levelMap.put(skill, level);
    }
}
