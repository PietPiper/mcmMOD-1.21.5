package pietpiper.mcmmod.debug;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.mixin.FishingBobberAccessor;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.config.SkillConfigManager;

import java.util.IdentityHashMap;
import java.util.Map;

public class FishingDebugManager {

    // One debug entity per bobber
    private static final Map<FishingBobberEntity, TextDisplayEntity> activeDisplays = new IdentityHashMap<>();

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.fishHook instanceof FishingBobberEntity bobber && !bobber.isRemoved()) {
                World world = player.getWorld();

                TextDisplayEntity display = activeDisplays.computeIfAbsent(bobber, b -> {
                    TextDisplayEntity d = new TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
                    d.setPosition(bobber.getX(), bobber.getY() + 1.5, bobber.getZ());
                    d.setBillboardMode(TextDisplayEntity.BillboardMode.VERTICAL);
                    world.spawnEntity(d);
                    return d;
                });
                FishingBobberAccessor accessor = (FishingBobberAccessor) bobber;

                int wait = accessor.getWaitCountdown();
                int travel = accessor.getFishTravelCountdown();
                int hook = accessor.getHookCountdown();

                int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
                int minBonus = SkillConfigManager.getTotalMinWaitReduction(level, player.hasVehicle());
                int maxBonus = SkillConfigManager.getTotalMaxWaitReduction(level, player.hasVehicle());

                String state = wait > 0 ? "WAITING" :
                        travel > 0 ? "TRAVELING" :
                                hook > 0 ? "HOOKED" : "IDLE";

                Text text = Text.literal("🎣 Fishing Debug")
                        .formatted(Formatting.BLACK)
                        .append(Text.literal("\nState: " + state).formatted(Formatting.RED))
                        .append(Text.literal("\nwaitCountdown: " + wait).formatted(Formatting.AQUA))
                        .append(Text.literal("\nfishTravelCountdown: " + travel).formatted(Formatting.GOLD))
                        .append(Text.literal("\nhookCountdown: " + hook).formatted(Formatting.BLUE))
                        .append(Text.literal("\nPlayer Level: " + level).formatted(Formatting.GREEN))
                        .append(Text.literal("\nMasterAngler: -" + minBonus + " to -" + maxBonus).formatted(Formatting.LIGHT_PURPLE));

                display.setText(text);
                display.setPosition(bobber.getPos().add(0, 1.5, 0));
            }
        }

        // Cleanup removed bobbers
        activeDisplays.entrySet().removeIf(entry -> {
            FishingBobberEntity bobber = entry.getKey();
            TextDisplayEntity display = entry.getValue();
            if (bobber.isRemoved() || !bobber.isAlive()) {
                display.discard();
                return true;
            }
            return false;
        });
    }

    // Dumb reflection hack for private fields like waitCountdown
    private static int getPrivate(FishingBobberEntity bobber, String fieldName) {
        try {
            var field = FishingBobberEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(bobber);
        } catch (Exception e) {
            return -1;
        }
    }
}