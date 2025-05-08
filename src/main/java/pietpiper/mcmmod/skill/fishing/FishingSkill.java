package pietpiper.mcmmod.skill.fishing;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.world.ServerWorld;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.XPUtil;

public class FishingSkill {
    public static final String NAME = "Fishing";

    public String getName() {
        return NAME;
    }

    /**
     * Called when a fish is caught by a player
     */
    public static void onGainXp(ServerPlayerEntity player, FishingBobberEntity bobber) {
    }
}