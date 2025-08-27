package pietpiper.mcmmod.objects;

public class PlayerSettings {
    //Player uuid identifier? I'm not sure how I will store these when its being referenced, possibly keep an updated map in PlayerDataManager? (Talk to Scott about query runtime cost)
    //Add xp bar toggle for each skill;
    public boolean showXpBar = true;

    //TODO: Add these to config for server wide toggle.
    public boolean showLevelUpMessages = true;
    public boolean enableActiveSkills = true;

    //Add colors for each XP bar from config values;
    public int fishingColor = 0x00FFFF;
}
