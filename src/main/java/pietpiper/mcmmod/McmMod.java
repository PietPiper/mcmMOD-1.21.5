package pietpiper.mcmmod;

import com.mojang.brigadier.CommandDispatcher;
import lombok.NoArgsConstructor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pietpiper.mcmmod.activity.DevCommandsActivity;
import pietpiper.mcmmod.activity.RegisterPlayerActivity;
import pietpiper.mcmmod.activity.requests.CommandRegistrationRequest;
import pietpiper.mcmmod.activity.utils.EventAdapter;
import pietpiper.mcmmod.guice.GuiceService;
import pietpiper.mcmmod.guice.modules.ModModule;
import pietpiper.mcmmod.persistence.db.DatabaseInitializer;

import static com.google.inject.Guice.createInjector;
import static pietpiper.mcmmod.constants.ModMetaData.MOD_ID;

@NoArgsConstructor(force = true)
public class McmMod implements ModInitializer {

	public static final Logger log = LogManager.getLogger(MOD_ID);

	private DatabaseInitializer databaseInitializer;
	private DevCommandsActivity devCommandsActivity;
	private RegisterPlayerActivity registerPlayerActivity;

	@Override
	public void onInitialize() {
		log.info("Initializing {}", MOD_ID);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			GuiceService.setInjector(createInjector(new ModModule(server)));
			GuiceService.getInjector().injectMembers(this);

			databaseInitializer = GuiceService.get(DatabaseInitializer.class);
			devCommandsActivity = GuiceService.get(DevCommandsActivity.class);
			registerPlayerActivity = GuiceService.get(RegisterPlayerActivity.class);

			databaseInitializer.initialize();
			registerCommands(server);

			ServerPlayConnectionEvents.JOIN.register(
							EventAdapter.adaptJoinEvent(registerPlayerActivity::execute)
			);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GuiceService.setInjector(null);
		});
	}

	/**
	 * Register commands for the server post server starting. This allows our injector to
	 * initialize.
	 *
	 * @param server The {@link MinecraftServer} that started.
	 */
	private void registerCommands(MinecraftServer server) {
		CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
		devCommandsActivity.execute(CommandRegistrationRequest.of(dispatcher));
	}
}