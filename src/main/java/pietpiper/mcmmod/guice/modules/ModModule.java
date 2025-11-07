package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;

/** Entry point for all module installations. **/
public class ModModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new MapperModule());
    install(new ConfigModule());
  }
}
