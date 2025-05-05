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
    //Add colors for each XP bar from config values;
    public int fishingColor = 0x00FFFF;
    public int tamingColor = 0xFFAA00;
    public int miningColor = 0xAAAAAA;
    public int acrobaticsColor = 0xFFFFFF;
    public int woodcuttingColor = 0x00AA00;
    public int herbalismColor = 0x55FF55;
    public int excavationColor = 0xFFFF55;
    public int unarmedColor = 0xAA00AA;
    public int archeryColor = 0xAA00AA;
    public int swordsColor = 0xAA0000;
    public int axesColor = 0x00AAAA;
    public int alchemyColor = 0xFF55FF;
    public int smeltingColor = 0x0000AA;
    public int enchantingColor = 0x5555FF;
    public int glidingColor = 0x000000;

    //Add other player specific settings here.
}
