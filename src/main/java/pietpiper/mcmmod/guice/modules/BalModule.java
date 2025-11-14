package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import pietpiper.mcmmod.bal.baos.PlayerBaoImpl;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;

/** Module for binding bal interfaces to implementations. */
public class BalModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlayerBao.class).to(PlayerBaoImpl.class);
  }
}
