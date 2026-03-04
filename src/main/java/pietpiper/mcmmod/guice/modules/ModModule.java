package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;

import java.time.Clock;

/** Entry point for all module installations. **/
@RequiredArgsConstructor
public class ModModule extends AbstractModule {

  private final MinecraftServer server;

  @Override
  protected void configure() {
    bind(Clock.class).toInstance(Clock.systemUTC());

    bind(MinecraftServer.class).toInstance(server);

    install(new ConfigModule());

    install(new DatabaseModule());

    install(new DalModule());
    install(new BalModule());
  }
}
