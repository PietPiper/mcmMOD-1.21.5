package pietpiper.mcmmod.mixin.fishingmix;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pietpiper.mcmmod.config.ConfigManager;
import pietpiper.mcmmod.config.SkillConfigManager;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.skill.fishing.FishingLootManager;
import pietpiper.mcmmod.skill.fishing.MagicFindManager;
import pietpiper.mcmmod.util.ServerReference;

import java.util.HashMap;
import java.util.Map;


@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin {

    public Map<PlayerEntity, Vec3d> castLocations = new HashMap<>();

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

            // Generate loot item based on level (adjust as needed)
            ItemStack loot = FishingLootManager.getLootForPlayer(serverPlayer, bobber, castLocations.get(bobber.getPlayerOwner()));
            // Run through Magic Find logic
            if (!loot.isEmpty()) {
                boolean didEnchant = MagicFindManager.tryApplyMagicFind(loot, serverPlayer);
                if(didEnchant) {
                    FishingLootManager.spawnTreasureParticles((ServerWorld) bobber.getPlayerOwner().getWorld(), castLocations.get(bobber.getPlayerOwner()), null, true);
                }
            }
            //Increase stat anyway even if they didn't catch an actual fish.
            if (!loot.isIn(ItemTags.FISHES)) {
                serverPlayer.increaseStat(Stats.FISH_CAUGHT, 1);
            }
            return ObjectArrayList.of(loot); // Replace vanilla loot with this item
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

            double riverFishingBonus = 0;
            BlockPos bobberPos = bobber.getBlockPos();
            RegistryEntry<Biome> biomeEntry = player.getWorld().getBiome(bobberPos);
            Identifier biomeId = player.getWorld().getRegistryManager()
                    .getOrThrow(RegistryKeys.BIOME)
                    .getId(biomeEntry.value());
            // Check if the biome is a river biome (using the default Minecraft ID)
            if (biomeId.equals(BiomeKeys.RIVER.getValue())) {
                ServerReference.logConsole("You are in a river biome!");
                riverFishingBonus = SkillConfigManager.getRiverBiomeBonus();
            } else {
                ServerReference.logConsole("Not a river biome. Current biome: " + biomeId);
            }

            int reduction = minReduction + bobber.getRandom().nextInt(maxReduction - minReduction + 1);
            if(ConfigManager.getConfig().debugMode) {
                ServerReference.logConsole("River bonus % : " + riverFishingBonus);
                ServerReference.logConsole("Because you are fishing in a river biome " + (this.waitCountdown - reduction) * (1 - riverFishingBonus/100.0) + " is your new cooldown instead of " + (this.waitCountdown - reduction));
            }
            this.waitCountdown = Math.max(20, (int) ((this.waitCountdown - reduction) * ( 1 - riverFishingBonus/100.0 )));
            castLocations.put(bobber.getPlayerOwner(), bobber.getPos());
        }
    }
    @Inject(
            method = "pullHookedEntity",
            at = @At("HEAD")
    )
    private void onShake(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity living)) return;

        Entity owner = ((FishingBobberEntity) (Object) this).getOwner();
        if (owner instanceof ServerPlayerEntity player) {
            int level = PlayerDataManager.getLevel(player.getUuid(), Skill.FISHING);
            double shakeChance = SkillConfigManager.getShakeChance(level);

            // Optional debug message
            ServerReference.logConsole("Shake Chance: " + String.format("%.1f", shakeChance * 100) + "%");

            if (player.getRandom().nextDouble() < shakeChance) {
                ServerReference.logConsole("Getting loot for the shake.");
                FishingLootManager.handleShakeDrops(living, player);
            }
        }
    }

}
