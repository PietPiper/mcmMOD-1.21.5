package pietpiper.mcmmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pietpiper.mcmmod.commands.DevCommands;
import pietpiper.mcmmod.guice.GuiceService;
import pietpiper.mcmmod.guice.modules.ModModule;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerDao;
import pietpiper.mcmmod.persistence.dal.models.Player;
import pietpiper.mcmmod.persistence.db.DatabaseInitializer;

import static com.google.inject.Guice.createInjector;
import static pietpiper.mcmmod.constants.ModConstants.MOD_ID;

public class McmMod implements ModInitializer {

	public static final Logger log = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		log.info("Initializing {}", MOD_ID);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			GuiceService.setInjector(createInjector(new ModModule(server)));
			GuiceService.get(DatabaseInitializer.class).initialize();
		});

		CommandRegistrationCallback.EVENT.register((dispatcher,
																								registryAccess,
																								environment) -> {
			DevCommands.register(dispatcher);
			log.info("Registered commands for {}", MOD_ID);
		});

		ServerPlayConnectionEvents.JOIN.register((handler,
																							sender,
																							server) -> {
			try {
				PlayerDao playerDao = GuiceService.get(PlayerDao.class);
				Player player = Player.builder()
								.id(handler.getPlayer().getUuid())
								.username(handler.getPlayer().getName().getString())
								.build();
				playerDao.registerPlayer(player);
			} catch (Exception e) {
				log.error("Failed to register player on join", e);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GuiceService.setInjector(null);
		});
	}
}