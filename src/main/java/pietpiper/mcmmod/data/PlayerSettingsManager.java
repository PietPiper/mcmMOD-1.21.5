package pietpiper.mcmmod.data;

import com.google.gson.Gson;

import pietpiper.mcmmod.config.ConfigManager;
import pietpiper.mcmmod.objects.PlayerSettings;
import pietpiper.mcmmod.skill.Skill;

import java.util.Objects;
import java.util.UUID;

public class PlayerSettingsManager {
    private static final Gson GSON = new Gson();

    public static PlayerSettings ConfiguredPlayerSettings() {
        PlayerSettings playerSettings = new PlayerSettings();
        if(ConfigManager.getConfig() != null) {
            playerSettings.fishingColor = ConfigManager.getConfig().defaultFishingColor;
        }
        return playerSettings;
    }

    public static PlayerSettings getSettings(UUID uuid) {
        return PlayerDataManager.getSettings(uuid);
    }

    public static void setSkillBarColor(UUID uuid, Skill skill, int colorHex) {
        PlayerSettings settings = PlayerDataManager.getSettings(uuid);
        if (Objects.requireNonNull(skill) == Skill.FISHING) {
            settings.fishingColor = colorHex;
        }
        PlayerDataManager.saveSettings(uuid, settings);
    }

    public static void setShowXpBar(UUID uuid, boolean show) {
        PlayerSettings settings = PlayerDataManager.getSettings(uuid);
        settings.showXpBar = show;
        PlayerDataManager.saveSettings(uuid, settings);
    }

    public static void setShowLevelUpMessages(UUID uuid, boolean show) {
        PlayerSettings settings = PlayerDataManager.getSettings(uuid);
        settings.showLevelUpMessages = show;
        PlayerDataManager.saveSettings(uuid, settings);
    }

    public static void setEnableActiveSkills(UUID uuid, boolean enable) {
        PlayerSettings settings = PlayerDataManager.getSettings(uuid);
        settings.enableActiveSkills = enable;
        PlayerDataManager.saveSettings(uuid, settings);
    }
}
