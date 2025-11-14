package pietpiper.mcmmod.persistence.dal.daos.interfaces;

import lombok.NonNull;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.Optional;
import java.util.UUID;

/** Player data access object interface. */
public interface PlayerDao {

  /**
   * Registers a {@link Player} into the players table.
   *
   * @param player The {@link Player} to register.
   */
  void addPlayer(@NonNull final Player player);

  /**
   * Deletes a player from the players table by their UUID.
   *
   * @param playerId The UUID of the player to delete.
   */
  void deletePlayer(@NonNull final UUID playerId);

  /**
   * Updates a player's username in the players table.
   *
   * @param playerId The UUID of the player to update.
   * @param newUsername The new username to set for the player.
   */
  void updatePlayerName(@NonNull final UUID playerId, @NonNull final String newUsername);

  /**
   * Retrieves a player by their UUID.
   *
   * @param playerId The UUID of the player to retrieve.
   * @return An Optional containing the player if found, empty otherwise.
   */
  Optional<Player> getPlayer(@NonNull final UUID playerId);
}