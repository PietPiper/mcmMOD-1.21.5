package pietpiper.mcmmod.activity.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.UUID;

import static pietpiper.mcmmod.McmMod.log;

/** Manager for handling player registration business logic. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RegisterPlayerManager {
  private final PlayerBao playerBao;

  /**
   * Registers a player with the given ID and username.
   *
   * @param playerId The UUID of the player
   * @param username The username of the player
   */
  public void registerPlayer(@NonNull UUID playerId, @NonNull String username) {
    try {
      final Player player = Player.builder()
              .id(playerId)
              .username(username)
              .build();

      playerBao.registerPlayer(player);
    } catch (Exception e) {
      log.error("Failed to register player: {} ({})", username, playerId, e);
    }
  }
}