package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import pietpiper.mcmmod.bal.baos.PlayerBaoImpl;
import pietpiper.mcmmod.bal.baos.PlayerSkillBaoImpl;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerSkillBao;

/** Module for binding bal interfaces to implementations. */
public class BalModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlayerBao.class).to(PlayerBaoImpl.class);
    bind(PlayerSkillBao.class).to(PlayerSkillBaoImpl.class);
  }
}
