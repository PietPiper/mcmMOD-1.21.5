package pietpiper.mcmmod.activity.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerSkillBao;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.Optional;
import java.util.UUID;

import static pietpiper.mcmmod.McmMod.log;

/** Manager for handling player registration business logic. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RegisterPlayerManager {

  private final PlayerBao playerBao;
  private final PlayerSkillBao playerSkillBao;

  /**
   * Registers or updates a player with the given ID and username.
   *
   * @param playerId The UUID of the player
   * @param username The username of the player
   */
  public void registerPlayer(@NonNull final UUID playerId,
                             @NonNull final String username) {

    try {
      final Optional<Player> existingPlayer = playerBao.getPlayer(playerId);

      if (existingPlayer.isEmpty()) {
        final Player player = Player.builder()
                .id(playerId)
                .username(username)
                .build();

        playerBao.registerPlayer(player);
        playerSkillBao.initializeSkills(playerId);

        log.info("Registered new player and initialized skills: {} ({})",
                username, playerId);
        return;
      }

      final Player current = existingPlayer.get();

      if (!current.getUsername().equals(username)) {
        playerBao.updatePlayerUsername(playerId, username);
        log.info("Updated username for player {} to {}", playerId, username);
      }

    } catch (Exception e) {
      log.error("Failed to register/update player: {} ({})",
              username, playerId, e);
    }
  }
}
