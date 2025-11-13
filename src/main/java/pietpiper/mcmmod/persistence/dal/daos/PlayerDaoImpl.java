package pietpiper.mcmmod.persistence.dal.daos;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbutils.QueryRunner;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerDao;
import pietpiper.mcmmod.persistence.dal.models.Player;
import pietpiper.mcmmod.persistence.db.utils.SQLiteResponseCodeUtils;

import java.sql.SQLException;

import static pietpiper.mcmmod.McmMod.log;


/** Implementation for a {@link PlayerDao}. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerDaoImpl implements PlayerDao {
  private final QueryRunner queryRunner;

  private static final String INSERT_PLAYER_SQL =
          "INSERT INTO players (id, username) VALUES (?, ?)";

  @Override
  public void registerPlayer(@NonNull final Player player) {
    try {
      int rowsAffected = queryRunner.update(
              INSERT_PLAYER_SQL,
              player.getId().toString(),
              player.getUsername()
      );

      if (rowsAffected > 0) {
        log.info("Successfully registered player: {} ({})",
                player.getUsername(),
                player.getId());
      }

    } catch (SQLException e) {
      if (SQLiteResponseCodeUtils.violatesConstraint(e.getErrorCode())) {
        log.info("Player already exists: {} ({})",
                player.getUsername(),
                player.getId());
      } else {
        log.error("Failed to register player: {} ({})",
                player.getUsername(),
                player.getId(),
                e);
      }
    }
  }
}