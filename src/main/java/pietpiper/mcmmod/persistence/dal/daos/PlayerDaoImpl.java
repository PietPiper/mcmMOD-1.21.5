package pietpiper.mcmmod.persistence.dal.daos;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerDao;
import pietpiper.mcmmod.persistence.dal.models.Player;
import pietpiper.mcmmod.persistence.db.utils.SQLiteResponseCodeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pietpiper.mcmmod.McmMod.log;

/** Implementation for a {@link PlayerDao}. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerDaoImpl implements PlayerDao {
  private final QueryRunner queryRunner;

  private static final String INSERT_PLAYER_SQL =
          "INSERT INTO players (id, username) VALUES (?, ?)";

  private static final String DELETE_PLAYER_SQL =
          "DELETE FROM players WHERE id = ?";

  private static final String UPDATE_PLAYER_NAME_SQL =
          "UPDATE players SET username = ? WHERE id = ?";

  private static final String SELECT_PLAYER_BY_ID_SQL =
          "SELECT id, username FROM players WHERE id = ?";

  private static final String SELECT_ALL_PLAYERS_SQL =
          "SELECT id, username FROM players ORDER BY username";

  @Override
  public void addPlayer(@NonNull final Player player) {
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

  @Override
  public void deletePlayer(@NonNull final UUID playerId) {
    try {
      int rowsAffected = queryRunner.update(
              DELETE_PLAYER_SQL,
              playerId.toString()
      );

      if (rowsAffected > 0) {
        log.info("Successfully deleted player with ID: {}", playerId);
      } else {
        log.info("No player found with ID: {}", playerId);
      }

    } catch (SQLException e) {
      log.error("Failed to delete player with ID: {}", playerId, e);
    }
  }

  @Override
  public void updatePlayerName(@NonNull final UUID playerId,
                               @NonNull final String newUsername) {
    try {
      int rowsAffected = queryRunner.update(
              UPDATE_PLAYER_NAME_SQL,
              newUsername,
              playerId.toString()
      );

      if (rowsAffected > 0) {
        log.info("Successfully updated player name for ID {}: {}",
                playerId,
                newUsername);
      } else {
        log.info("No player found with ID: {}", playerId);
      }

    } catch (SQLException e) {
      log.error("Failed to update player name for ID {}: {}",
              playerId,
              newUsername,
              e);
    }
  }

  @Override
  public Optional<Player> getPlayer(@NonNull final UUID playerId) {
    try {
      Player player = queryRunner.query(
              SELECT_PLAYER_BY_ID_SQL,
              playerResultSetHandler,
              playerId.toString()
      );

      if (player != null) {
        log.debug("Successfully retrieved player: {} ({})",
                player.getUsername(),
                player.getId());
        return Optional.of(player);
      } else {
        log.debug("No player found with ID: {}", playerId);
        return Optional.empty();
      }

    } catch (SQLException e) {
      log.error("Failed to retrieve player with ID: {}", playerId, e);
      return Optional.empty();
    }
  }

  @Override
  public List<Player> listPlayers() {
    try {
      List<Player> players = queryRunner.query(
              SELECT_ALL_PLAYERS_SQL,
              playersResultSetHandler
      );

      log.debug("Successfully retrieved {} players", players != null ? players.size() : 0);
      return players != null ? players : new ArrayList<>();

    } catch (SQLException e) {
      log.error("Failed to retrieve player list", e);
      return new ArrayList<>();
    }
  }

  private final ResultSetHandler<Player> playerResultSetHandler = (ResultSet rs) -> {
    if (rs.next()) {
      final UUID id = UUID.fromString(rs.getString("id"));
      final String username = rs.getString("username");
      return Player.builder()
              .id(id)
              .username(username)
              .build();
    }
    return null;
  };

  private final ResultSetHandler<List<Player>> playersResultSetHandler = (ResultSet rs) -> {
    List<Player> players = new ArrayList<>();
    while (rs.next()) {
      final UUID id = UUID.fromString(rs.getString("id"));
      final String username = rs.getString("username");
      Player player = Player.builder()
              .id(id)
              .username(username)
              .build();
      players.add(player);
    }
    return players;
  };
}