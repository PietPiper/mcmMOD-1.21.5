package pietpiper.mcmmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pietpiper.mcmmod.config.server.ServerSettings;
import pietpiper.mcmmod.config.skill.SkillConfigs;
import pietpiper.mcmmod.guice.GuiceService;
import pietpiper.mcmmod.guice.modules.ModModule;

import static com.google.inject.Guice.createInjector;
import static pietpiper.mcmmod.constants.ModConstants.MOD_ID;

public class McmMod implements ModInitializer {

	public static final Logger log = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		log.info("Initializing {}", MOD_ID);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			GuiceService.setInjector(createInjector(new ModModule()));
			checkConfig();
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GuiceService.setInjector(null);
		});
	}

	private void checkConfig() {
		final ServerSettings serverSettings = GuiceService.get(ServerSettings.class);
		final SkillConfigs skilLConfigs = GuiceService.get(SkillConfigs.class);
	}
}