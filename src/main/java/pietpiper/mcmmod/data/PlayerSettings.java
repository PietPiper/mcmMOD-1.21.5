package pietpiper.mcmmod.data;

import pietpiper.mcmmod.config.ConfigManager;

import java.io.ObjectInputFilter;

public class PlayerSettings {
    //Player uuid identifier? I'm not sure how I will store these when its being referenced, possibly keep an updated map in PlayerDataManager? (Talk to Scott about query runtime cost)
    //Add xp bar toggle for each skill;
    public boolean showXpBar = true;
    //TODO: Add these to config for server wide toggle.
    public boolean showLevelUpMessages = true;
    public boolean enableActiveSkills = true;
    //Add colors for each XP bar from Skill enumerated values;
    public int fishingColor = ConfigManager.getConfig().defaultFishingColor;
    public int tamingColor = ConfigManager.getConfig().defaultTamingColor;
    public int miningColor = ConfigManager.getConfig().defaultMiningColor;
    public int acrobaticsColor = ConfigManager.getConfig().defaultAcrobaticsColor;
    public int woodcuttingColor = ConfigManager.getConfig().defaultWoodcuttingColor;
    public int herbalismColor = ConfigManager.getConfig().defaultHerbalismColor;
    public int excavationColor = ConfigManager.getConfig().defaultExcavationColor;
    public int unarmedColor = ConfigManager.getConfig().defaultUnarmedColor;
    public int archeryColor = ConfigManager.getConfig().defaultArcheryColor;
    public int swordsColor = ConfigManager.getConfig().defaultSwordsColor;
    public int axesColor = ConfigManager.getConfig().defaultAxesColor;
    public int alchemyColor = ConfigManager.getConfig().defaultAlchemyColor;
    public int smeltingColor = ConfigManager.getConfig().defaultSmeltingColor;
    public int enchantingColor = ConfigManager.getConfig().defaultEnchantingColor;
    public int glidingColor = ConfigManager.getConfig().defaultGlidingColor;
    //Add other player specific settings here.
}
