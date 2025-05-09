package pietpiper.mcmmod.config;

// If values change make sure to update starting config file string as well. Values in here
// are never actually referenced in code unless the config file fails to be loaded.
// Also update default values in Player Settings for individual settings like colors.
public class McmmodConfig {
    public int startingLevel = 0;
    public boolean debugMode = false;
    public boolean showXpBar = true;
    public boolean enableActiveSkills = true;
    public boolean enableFishing = true;
    public boolean enableTaming = true;
    public boolean enableMining = true;
    public boolean enableAcrobatics = true;
    public boolean enableWoodcutting = true;
    public boolean enableHerbalism = true;
    public boolean enableExcavation = true;
    public boolean enableUnarmed = true;
    public boolean enableArchery = true;
    public boolean enableSwords = true;
    public boolean enableAxes = true;
    public boolean enableAlchemy = true;
    public boolean enableSmelting = true;
    public boolean enableEnchanting = true;
    public boolean enableGliding = true;
    public int maxLevel = Integer.MAX_VALUE;
    public int defaultFishingColor = 0x00FFFF;
    public int defaultTamingColor = 0xFFAA00;
    public int defaultMiningColor = 0xAAAAAA;
    public int defaultAcrobaticsColor = 0xFFFFFF;
    public int defaultWoodcuttingColor = 0x00AA00;
    public int defaultHerbalismColor = 0x55FF55;
    public int defaultExcavationColor = 0xFFFF55;
    public int defaultUnarmedColor = 0xAA00AA;
    public int defaultArcheryColor = 0xAA00AA;
    public int defaultSwordsColor = 0xAA0000;
    public int defaultAxesColor = 0x00AAAA;
    public int defaultAlchemyColor = 0xFF55FF;
    public int defaultSmeltingColor = 0x0000AA;
    public int defaultEnchantingColor = 0x5555FF;
    public int defaultGlidingColor = 0x000000;
    // Optional: add getters/setters if you want more control
}
