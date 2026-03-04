package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import pietpiper.mcmmod.persistence.dal.daos.PlayerDaoImpl;
import pietpiper.mcmmod.persistence.dal.daos.PlayerSkillDaoImpl;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerDao;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerSkillDao;

/** Module for binding dal interfaces to implementations. */
public class DalModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlayerDao.class).to(PlayerDaoImpl.class);
    bind(PlayerSkillDao.class).to(PlayerSkillDaoImpl.class);
  }
}
