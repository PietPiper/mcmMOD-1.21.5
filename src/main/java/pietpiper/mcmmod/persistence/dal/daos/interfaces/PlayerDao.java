package pietpiper.mcmmod.persistence.dal.daos.interfaces;

import lombok.NonNull;
import pietpiper.mcmmod.persistence.dal.models.Player;

/** Player data access object interface. */
public interface PlayerDao {

  /**
   * Registers a {@link Player} into the players table.
   *
   * @param player The {@link Player} to register.
   */
  void registerPlayer(@NonNull final Player player);
}
