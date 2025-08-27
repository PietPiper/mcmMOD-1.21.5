package pietpiper.mcmmod.objects;

// If values change make sure to update starting config file string as well. Values in here
// are never actually referenced in code unless the config file fails to be loaded.
// Also update default values in Player Settings for individual settings like colors.
public class McmmodConfig {
    public int startingLevel = 0;
    public boolean debugMode = false;
    public int maxLevel = Integer.MAX_VALUE;
    public int defaultFishingColor = 0x00FFFF;
}
