package pietpiper.mcmmod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;

@Mixin(FishingBobberEntity.class)
public abstract class IceFishingMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkForIceFishing(CallbackInfo ci) {
        FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;

        if (!bobber.getWorld().isClient) {
            Entity owner = bobber.getOwner();
            if (owner instanceof ServerPlayerEntity player) {
                World world = bobber.getWorld();
                BlockPos bobberPos = bobber.getBlockPos();
                BlockPos below = bobberPos.down();

                BlockState belowState = world.getBlockState(below);
                if (belowState.isOf(Blocks.ICE) || belowState.isOf(Blocks.PACKED_ICE) || belowState.isOf(Blocks.BLUE_ICE)) {
                    int playerLevel = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
                    int requiredLevel = SkillConfigManager.getIceFishingLevel();

                    if (playerLevel >= requiredLevel) {
                        world.setBlockState(below, Blocks.WATER.getDefaultState());
                    }
                }
            }
        }
    }
}
