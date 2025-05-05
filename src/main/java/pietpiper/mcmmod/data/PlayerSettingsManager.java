package pietpiper.mcmmod.data;

import com.google.gson.Gson;
import org.lwjgl.system.Platform;
import pietpiper.mcmmod.config.ConfigManager;
import pietpiper.mcmmod.util.ServerReference;
import pietpiper.mcmmod.skill.Skill;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerSettingsManager {
    private static final Gson GSON = new Gson();

    public static PlayerSettings ConfiguredPlayerSettings() {
        PlayerSettings playerSettings = new PlayerSettings();
        if(ConfigManager.getConfig() != null) {
            playerSettings.fishingColor = ConfigManager.getConfig().defaultFishingColor;
            playerSettings.tamingColor = ConfigManager.getConfig().defaultTamingColor;
            playerSettings.miningColor = ConfigManager.getConfig().defaultMiningColor;
            playerSettings.acrobaticsColor = ConfigManager.getConfig().defaultAcrobaticsColor;
            playerSettings.woodcuttingColor = ConfigManager.getConfig().defaultWoodcuttingColor;
            playerSettings.herbalismColor = ConfigManager.getConfig().defaultHerbalismColor;
            playerSettings.excavationColor = ConfigManager.getConfig().defaultExcavationColor;
            playerSettings.unarmedColor = ConfigManager.getConfig().defaultUnarmedColor;
            playerSettings.archeryColor = ConfigManager.getConfig().defaultArcheryColor;
            playerSettings.swordsColor = ConfigManager.getConfig().defaultSwordsColor;
            playerSettings.axesColor = ConfigManager.getConfig().defaultAxesColor;
            playerSettings.alchemyColor = ConfigManager.getConfig().defaultAlchemyColor;
            playerSettings.smeltingColor = ConfigManager.getConfig().defaultSmeltingColor;
            playerSettings.enchantingColor = ConfigManager.getConfig().defaultEnchantingColor;
            playerSettings.glidingColor = ConfigManager.getConfig().defaultGlidingColor;
        }
        return playerSettings;
    }

    public static PlayerSettings getSettings(UUID uuid) {
        return PlayerDataManager.getSettings(uuid);
    }

    public static void setSkillBarColor(UUID uuid, Skill skill, int colorHex) {
        PlayerSettings settings = PlayerDataManager.getSettings(uuid);
        switch (skill) {
            case FISHING -> settings.fishingColor = colorHex;
            case TAMING -> settings.tamingColor = colorHex;
            case MINING -> settings.miningColor = colorHex;
            case ACROBATICS -> settings.acrobaticsColor = colorHex;
            case WOODCUTTING -> settings.woodcuttingColor = colorHex;
            case HERBALISM -> settings.herbalismColor = colorHex;
            case EXCAVATION -> settings.excavationColor = colorHex;
            case UNARMED -> settings.unarmedColor = colorHex;
            case ARCHERY -> settings.archeryColor = colorHex;
            case SWORDS -> settings.swordsColor = colorHex;
            case AXES -> settings.axesColor = colorHex;
            case ALCHEMY -> settings.alchemyColor = colorHex;
            case SMELTING -> settings.smeltingColor = colorHex;
            case ENCHANTING -> settings.enchantingColor = colorHex;
            case GLIDING -> settings.glidingColor = colorHex;
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
