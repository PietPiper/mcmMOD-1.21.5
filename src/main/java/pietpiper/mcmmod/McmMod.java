package pietpiper.mcmmod;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pietpiper.mcmmod.guice.modules.ModModule;

import static pietpiper.mcmmod.constants.ModConstants.MOD_ID;

public class McmMod implements ModInitializer {

	public static final Logger log = LogManager.getLogger(MOD_ID);
	private static Injector injector;

	@Override
	public void onInitialize() {

		injector = Guice.createInjector(new ModModule());
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {

		});
	}
}