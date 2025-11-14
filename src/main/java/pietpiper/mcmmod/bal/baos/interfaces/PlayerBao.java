package pietpiper.mcmmod.bal.baos.interfaces;

import lombok.NonNull;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.Optional;
import java.util.UUID;

/** Player business access object interface. */
public interface PlayerBao {

  /**
   * Registers a new player in the system.
   *
   * @param player The player to register
   */
  void registerPlayer(@NonNull final Player player);

  /**
   * Retrieves a player by their UUID.
   *
   * @param playerId The UUID of the player to retrieve
   * @return An Optional containing the player if found, empty otherwise
   */
  Optional<Player> getPlayer(@NonNull final UUID playerId);

  /**
   * Updates a player's username.
   *
   * @param playerId The UUID of the player to update
   * @param newUsername The new username to set
   * @return true if the update was successful, false otherwise
   */
  boolean updatePlayerUsername(@NonNull final UUID playerId,
                               @NonNull final String newUsername);

  /**
   * Deletes a player from the system.
   *
   * @param playerId The UUID of the player to delete
   * @return true if the deletion was successful, false otherwise
   */
  boolean deletePlayer(@NonNull final UUID playerId);

  /**
   * Checks if a player exists by their UUID.
   *
   * @param playerId The UUID to check
   * @return true if the player exists, false otherwise
   */
  boolean playerExists(@NonNull final UUID playerId);
}