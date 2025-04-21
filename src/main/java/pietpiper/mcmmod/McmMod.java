package pietpiper.mcmmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.util.ServerReference;

import java.util.UUID;

public class McmMod implements ModInitializer {
	public static final String MOD_ID = "mcmmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		//Initialze or connect database.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SQLiteManager.connect(server);
			ServerReference.setServer(server);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			UUID uuid = player.getUuid();

			//Initialize the player.
			PlayerDataManager.initPlayer(uuid);

			ServerReference.broadcast("Initialized player data for " + player.getName());
		});


	}


}