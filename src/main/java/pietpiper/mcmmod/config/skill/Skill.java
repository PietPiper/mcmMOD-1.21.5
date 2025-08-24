package pietpiper.mcmmod.config.skill;

import java.awt.Color;

public enum Skill {
    FISHING(Color.CYAN),
    TAMING(new Color(0xFFAA00)),
    MINING(new Color(0xAAAAAA)),
    ACROBATICS(Color.WHITE),
    WOODCUTTING(new Color(0x00AA00)),
    HERBALISM(new Color(0x55FF55)),
    EXCAVATION(new Color(0xFFFF55)),
    UNARMED(new Color(0xAA00AA)),
    ARCHERY(new Color(0xAA00AA)),
    SWORDS(new Color(0xAA0000)),
    AXES(new Color(0x00AAAA)),
    ALCHEMY(new Color(0xFF55FF)),
    SMELTING(new Color(0x0000AA)),
    ENCHANTING(new Color(0x5555FF)),
    GLIDING(new Color(0x000000));

    private final Color defaultColor;

    Skill(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public SkillConfig defaultConfig() {
        return SkillConfig.builder()
                .enabled(true)
                .color(defaultColor)
                .build();
    }
}
