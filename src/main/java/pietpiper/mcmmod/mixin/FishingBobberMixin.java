package pietpiper.mcmmod.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.skill.fishing.FishingLootManager;
import pietpiper.mcmmod.skill.fishing.MagicFindManager;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin {

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
            )
    )
    private ObjectArrayList<ItemStack> replaceFishingLoot(LootTable instance, LootWorldContext parameters) {
        // Get player and XP
        FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;

        if (bobber.getPlayerOwner() instanceof ServerPlayerEntity serverPlayer) {
            // Award XP (optional here if not done elsewhere)
            // FishingSkill.onGainXp(serverPlayer, bobber);

            // Generate treasure item based on level (adjust as needed)
            ItemStack treasure = FishingLootManager.getLootForPlayer(serverPlayer);
            // Run through Magic Find logic
            if (!treasure.isEmpty()) {
                MagicFindManager.tryApplyMagicFind(treasure, serverPlayer);
            }
            return ObjectArrayList.of(treasure); // Replace vanilla loot with just this item
        }

        // Fallback to no loot if not a server player
        return ObjectArrayList.of();
    }

    @Shadow
    private int waitCountdown;

    @Inject(
            method = "tickFishingLogic",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;waitCountdown:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private void injectMasterAnglerReduction(BlockPos pos, CallbackInfo ci) {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object)this;
        PlayerEntity player = bobber.getPlayerOwner();
        if (player != null) {
            int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
            boolean inBoat = player.hasVehicle() && player.getVehicle() instanceof BoatEntity;

            int minReduction = SkillConfigManager.getTotalMinWaitReduction(level, inBoat);
            int maxReduction = SkillConfigManager.getTotalMaxWaitReduction(level, inBoat);

            if (maxReduction < minReduction) {
                int temp = minReduction;
                minReduction = maxReduction;
                maxReduction = temp;
            }

            int reduction = minReduction + bobber.getRandom().nextInt(maxReduction - minReduction + 1);
            this.waitCountdown = Math.max(20, this.waitCountdown - reduction);

        }
    }

}
