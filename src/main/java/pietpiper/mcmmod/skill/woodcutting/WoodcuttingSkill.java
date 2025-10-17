package pietpiper.mcmmod.skill.mining;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import pietpiper.mcmmod.config.MiningConfig;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.XPUtil;

public class WoodcuttingSkill {

    public static void handleMinedBlock(PlayerEntity player, BlockState state) {
        if(!(player instanceof  ServerPlayerEntity)) {
            return;
        }
        Block block = state.getBlock();

        // Only grant XP if the block is NOT player-placed
        int xp = WoodcuttingConfig.getXPForBlock(block);
        if (xp > 0) {
            System.out.println("You just got " + xp + " woodcutting XP.");
            XPUtil.addXP(player.getUuid(), Skill.WOODCUTTING, xp);
        }
    }
}