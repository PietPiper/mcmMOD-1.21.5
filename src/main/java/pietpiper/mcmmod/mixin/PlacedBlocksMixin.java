package pietpiper.mcmmod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pietpiper.mcmmod.data.PlacedBlockDatabaseManager;
import pietpiper.mcmmod.util.ServerReference;

@Mixin(BlockItem.class)
public class PlacedBlocksMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue() != ActionResult.SUCCESS) return;

        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ServerReference.logConsole("A block has been placed.");
        if (!world.isClient && player instanceof ServerPlayerEntity && world instanceof ServerWorld serverWorld) {
            PlacedBlockDatabaseManager.markPlaced(serverWorld, pos);
        }
    }
}
