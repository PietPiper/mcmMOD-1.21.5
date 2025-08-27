package pietpiper.mcmmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pietpiper.mcmmod.command.DevCommandHandler;
import pietpiper.mcmmod.config.*;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.data.PrimaryDatabaseManager;
import pietpiper.mcmmod.debug.FishingDebugManager;
import pietpiper.mcmmod.util.ServerReference;

import java.util.UUID;

public class McmMod implements ModInitializer {
	private static final String MOD_ID = "mcmmod";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		//Initialze server reference for message broadcasting, load the config, initialize or connect database IN THAT ORDER.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ConfigManager.load();
			SkillConfigManager.load();
			FishingConfig.loadConfig();
			PrimaryDatabaseManager.connect(server);
			ServerReference.setServer(server);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			UUID uuid = player.getUuid();
			PlayerDataManager.initPlayer(uuid);
			ServerReference.logConsole("Initialized player data for " + player.getName());
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			DevCommandHandler.register(dispatcher);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if(ConfigManager.getConfig().debugMode) {
				FishingDebugManager.tick(server);
			}
		});
	}


}