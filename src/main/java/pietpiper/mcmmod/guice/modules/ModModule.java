package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;

/** Entry point for all module installations. **/
@RequiredArgsConstructor
public class ModModule extends AbstractModule {

  private final MinecraftServer server;

  @Override
  protected void configure() {
    bind(MinecraftServer.class).toInstance(server);

    install(new ConfigModule());
    install(new DatabaseModule());

    install(new DalModule());
  }
}
