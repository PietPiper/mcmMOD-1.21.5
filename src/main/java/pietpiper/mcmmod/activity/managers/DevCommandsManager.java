package pietpiper.mcmmod.activity.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.context.CommandContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pietpiper.mcmmod.bal.PlayerIdentityBAL;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Manager for handling developer commands workflow. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DevCommandsManager {

  private final PlayerIdentityBAL playerIdentityBAL;
  private final RegisterPlayerManager registerPlayerManager;
  private final PlayerBao playerBao;

  /**
   * Registers a new player in the system.
   *
   * @param context The command context
   * @param username The username to register
   * @return 1 on success
   */
  public int registerPlayer(@NonNull final CommandContext<ServerCommandSource> context,
                            @NonNull final String username) {

    final Optional<UUID> uuidOptional = playerIdentityBAL.resolveUuid(username);
    if (uuidOptional.isEmpty()) {
      sendError(context, "Username not found: " + username);
      return 0;
    }

    final UUID playerId = uuidOptional.get();
    registerPlayerManager.registerPlayer(playerId, username);

    sendSuccess(context,
            "Registered player: " + username + " (" + playerId + ")");

    return 1;
  }

  /**
   * Retrieves and displays player information by ID.
   *
   * @param context The command context
   * @param playerIdStr The player ID as a string
   * @return 1 on completion
   */
  public int getPlayer(@NonNull final CommandContext<ServerCommandSource> context,
                       @NonNull final String playerIdStr) {
    return withValidPlayerId(context, playerIdStr, playerId ->
            playerBao.getPlayer(playerId).ifPresentOrElse(
                    player -> sendSuccess(context,
                            "Player found: " + player.getUsername() + " (" + player.getId() + ")"),
                    () -> sendError(context, "Player not found with ID: " + playerIdStr)
            )
    );
  }

  /**
   * Updates a player's username.
   *
   * @param context The command context
   * @param playerIdStr The player ID as a string
   * @param newUsername The new username to set
   * @return 1 on completion
   */
  public int updatePlayer(@NonNull final CommandContext<ServerCommandSource> context,
                          @NonNull final String playerIdStr,
                          @NonNull final String newUsername) {
    return withValidPlayerId(context, playerIdStr, playerId -> {
      boolean success = playerBao.updatePlayerUsername(playerId, newUsername);
      if (success) {
        sendSuccess(context, "Updated player " + playerIdStr + " to: " + newUsername);
      } else {
        sendError(context, "Failed to update player " + playerIdStr);
      }
    });
  }

  /**
   * Deletes a player from the system.
   *
   * @param context The command context
   * @param playerIdStr The player ID as a string
   * @return 1 on completion
   */
  public int deletePlayer(@NonNull final CommandContext<ServerCommandSource> context,
                          @NonNull final String playerIdStr) {
    return withValidPlayerId(context, playerIdStr, playerId -> {
      boolean success = playerBao.deletePlayer(playerId);
      if (success) {
        sendSuccess(context, "Deleted player with ID: " + playerIdStr);
      } else {
        sendError(context, "Failed to delete player with ID: " + playerIdStr);
      }
    });
  }

  /**
   * Lists all players in the database.
   *
   * @param context The command context
   * @return 1 on completion
   */
  public int listPlayers(@NonNull final CommandContext<ServerCommandSource> context) {
    List<Player> players = playerBao.listPlayers();

    if (players.isEmpty()) {
      sendWarning(context, "No players found in database");
    } else {
      sendSuccess(context, "Players in database (" + players.size() + "):");
      players.forEach(player ->
              sendFeedback(context, "§7- " + player.getUsername() + " §8(" + player.getId() + ")")
      );
    }
    return 1;
  }

  /**
   * Executes an action with a validated player UUID.
   *
   * @param context The command context
   * @param playerIdStr The player ID string to validate
   * @param action The action to execute with the validated UUID
   * @return 1 on completion
   */
  private int withValidPlayerId(@NonNull final CommandContext<ServerCommandSource> context,
                                @NonNull final String playerIdStr,
                                @NonNull final Consumer<UUID> action) {
    try {
      UUID playerId = UUID.fromString(playerIdStr);
      action.accept(playerId);
      return 1;
    } catch (IllegalArgumentException e) {
      sendError(context, "Invalid UUID format: " + playerIdStr);
      return 1;
    }
  }

  /**
   * Sends feedback message to the command source.
   *
   * @param context The command context
   * @param message The message to send
   */
  private void sendFeedback(@NonNull final CommandContext<ServerCommandSource> context,
                            @NonNull final String message) {
    context.getSource().sendFeedback(() -> Text.literal(message), false);
  }

  /**
   * Sends success feedback message to the command source.
   *
   * @param context The command context
   * @param message The success message to send
   */
  private void sendSuccess(@NonNull final CommandContext<ServerCommandSource> context,
                           @NonNull final String message) {
    sendFeedback(context, "§a" + message);
  }

  /**
   * Sends error feedback message to the command source.
   *
   * @param context The command context
   * @param message The error message to send
   */
  private void sendError(@NonNull final CommandContext<ServerCommandSource> context,
                         @NonNull final String message) {
    sendFeedback(context, "§c" + message);
  }

  /**
   * Sends warning feedback message to the command source.
   *
   * @param context The command context
   * @param message The warning message to send
   */
  private void sendWarning(@NonNull final CommandContext<ServerCommandSource> context,
                           @NonNull final String message) {
    sendFeedback(context, "§e" + message);
  }
}
