package pietpiper.mcmmod.skill;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractSkill {
    public abstract String getName();

    public abstract void onGainXp(ServerPlayerEntity player);
}
