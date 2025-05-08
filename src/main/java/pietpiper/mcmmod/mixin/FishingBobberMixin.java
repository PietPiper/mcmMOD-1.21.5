package pietpiper.mcmmod.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pietpiper.mcmmod.skill.fishing.FishingLootManager;

import java.util.List;

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
            return ObjectArrayList.of(treasure); // Replace vanilla loot with just this item
        }

        // Fallback to no loot if not a server player
        return ObjectArrayList.of();
    }
}
