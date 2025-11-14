package pietpiper.mcmmod.bal.baos;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerDao;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.Optional;
import java.util.UUID;

import static pietpiper.mcmmod.McmMod.log;

/** Implementation for a {@link PlayerBao}. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerBaoImpl implements PlayerBao {
  private final PlayerDao playerDao;

  @Override
  public void registerPlayer(@NonNull final Player player) {
    if (player.getUsername().trim().isEmpty()) {
      log.error("Cannot register player with empty username");
      return;
    }

    if (playerExists(player.getId())) {
      log.info("Player already registered: {} ({})", player.getUsername(), player.getId());
      return;
    }

    playerDao.addPlayer(player);
  }

  @Override
  public Optional<Player> getPlayer(@NonNull final UUID playerId) {
    try {
      return playerDao.getPlayer(playerId);
    } catch (Exception e) {
      log.error("Error retrieving player with ID: {}", playerId, e);
      return Optional.empty();
    }
  }

  @Override
  public boolean updatePlayerUsername(@NonNull final UUID playerId, @NonNull final String newUsername) {
    if (newUsername.trim().isEmpty()) {
      log.error("Cannot update player username to empty string for player ID: {}", playerId);
      return false;
    }

    if (!playerExists(playerId)) {
      log.error("Cannot update username - player not found with ID: {}", playerId);
      return false;
    }

    try {
      playerDao.updatePlayerName(playerId, newUsername);
      return true;
    } catch (Exception e) {
      log.error("Failed to update username for player ID: {} to {}", playerId, newUsername, e);
      return false;
    }
  }

  @Override
  public boolean deletePlayer(@NonNull final UUID playerId) {
    if (!playerExists(playerId)) {
      log.warn("Cannot delete - player not found with ID: {}", playerId);
      return false;
    }

    try {
      playerDao.deletePlayer(playerId);
      return true;
    } catch (Exception e) {
      log.error("Failed to delete player with ID: {}", playerId, e);
      return false;
    }
  }

  @Override
  public boolean playerExists(@NonNull final UUID playerId) {
    try {
      return playerDao.getPlayer(playerId).isPresent();
    } catch (Exception e) {
      log.error("Error checking player existence for ID: {}", playerId, e);
      return false;
    }
  }
}