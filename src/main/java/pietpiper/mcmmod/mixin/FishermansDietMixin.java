package pietpiper.mcmmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;

@Mixin(ItemStack.class)
public abstract class FishermansDietMixin {

    @Inject(method = "finishUsing", at = @At("RETURN"), cancellable = true)
    private void applyFishermansDiet(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        System.out.println("[FishingDietMixin] you ate something!");
        if (!(user instanceof PlayerEntity player) || world.isClient) return;

        ItemStack stack = (ItemStack) (Object) this;
        Item item = stack.getItem();

        if (item == Items.COOKED_COD || item == Items.COOKED_SALMON) {
            int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
            SkillConfigManager.FishermansDietBonus bonus = SkillConfigManager.getFishermansDietBonus(level);

            if (bonus != null) {
                player.getHungerManager().add(bonus.hunger, bonus.saturation);
            }
        }
    }
}
