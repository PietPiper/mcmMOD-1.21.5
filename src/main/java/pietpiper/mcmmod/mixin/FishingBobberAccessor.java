package pietpiper.mcmmod.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberAccessor {
    @Accessor("waitCountdown")
    int getWaitCountdown();

    @Accessor("fishTravelCountdown")
    int getFishTravelCountdown();

    @Accessor("hookCountdown")
    int getHookCountdown();
}
