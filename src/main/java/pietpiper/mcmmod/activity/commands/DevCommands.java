package pietpiper.mcmmod.activity.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.persistence.dal.models.Player;

import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Activity for handling developer/admin commands workflow. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DevCommands {

  private final PlayerBao playerBao;

  /**
   * Registers all developer/admin commands with the command dispatcher.
   *
   * @param dispatcher The command dispatcher to register commands with
   */
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(literal("admin")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("player")
                    .then(literal("register")
                            .then(argument("username", StringArgumentType.string())
                                    .executes(context -> registerPlayer(
                                            context,
                                            StringArgumentType.getString(context, "username")
                                    ))
                            )
                    )
                    .then(literal("get")
                            .then(argument("playerId", StringArgumentType.string())
                                    .executes(context -> getPlayer(
                                            context,
                                            StringArgumentType.getString(context, "playerId")
                                    ))
                            )
                    )
                    .then(literal("update")
                            .then(argument("playerId", StringArgumentType.string())
                                    .then(argument("newUsername", StringArgumentType.string())
                                            .executes(context -> updatePlayer(
                                                    context,
                                                    StringArgumentType.getString(context, "playerId"),
                                                    StringArgumentType.getString(context, "newUsername")
                                            ))
                                    )
                            )
                    )
                    .then(literal("delete")
                            .then(argument("playerId", StringArgumentType.string())
                                    .executes(context -> deletePlayer(
                                            context,
                                            StringArgumentType.getString(context, "playerId")
                                    ))
                            )
                    )
                    .then(literal("list")
                            .executes(this::listPlayers)
                    )
            )
    );
  }

  /**
   * Registers a new player in the system.
   *
   * @param context The command context
   * @param username The username to register
   * @return 1 on success
   */
  private int registerPlayer(CommandContext<ServerCommandSource> context, String username) {
    UUID playerId = UUID.randomUUID();

    Player player = Player.builder()
            .id(playerId)
            .username(username)
            .build();

    playerBao.registerPlayer(player);

    context.getSource().sendFeedback(
            () -> Text.literal("§aRegistered player: " + username + " (" + playerId + ")"),
            false
    );
    return 1;
  }

  /**
   * Retrieves and displays player information by ID.
   *
   * @param context The command context
   * @param playerIdStr The player ID as a string
   * @return 1 on completion
   */
  private int getPlayer(CommandContext<ServerCommandSource> context, String playerIdStr) {
    try {
      UUID playerId = UUID.fromString(playerIdStr);
      var playerOpt = playerBao.getPlayer(playerId);

      if (playerOpt.isPresent()) {
        Player player = playerOpt.get();
        context.getSource().sendFeedback(
                () -> Text.literal("§aPlayer found: " + player.getUsername() + " (" + player.getId() + ")"),
                false
        );
      } else {
        context.getSource().sendFeedback(
                () -> Text.literal("§cPlayer not found with ID: " + playerIdStr),
                false
        );
      }
    } catch (IllegalArgumentException e) {
      context.getSource().sendFeedback(
              () -> Text.literal("§cInvalid UUID format: " + playerIdStr),
              false
      );
    }
    return 1;
  }

  /**
   * Updates a player's username.
   *
   * @param context The command context
   * @param playerIdStr The player ID as a string
   * @param newUsername The new username to set
   * @return 1 on completion
   */
  private int updatePlayer(CommandContext<ServerCommandSource> context, String playerIdStr, String newUsername) {
    try {
      UUID playerId = UUID.fromString(playerIdStr);
      boolean success = playerBao.updatePlayerUsername(playerId, newUsername);

      if (success) {
        context.getSource().sendFeedback(
                () -> Text.literal("§aUpdated player " + playerIdStr + " to: " + newUsername),
                false
        );
      } else {
        context.getSource().sendFeedback(
                () -> Text.literal("§cFailed to update player " + playerIdStr),
                false
        );
      }
    } catch (IllegalArgumentException e) {
      context.getSource().sendFeedback(
              () -> Text.literal("§cInvalid UUID format: " + playerIdStr),
              false
      );
    }
    return 1;
  }

  /**
   * Deletes a player from the system.
   *
   * @param context The command context
   * @param playerIdStr The player ID as a string
   * @return 1 on completion
   */
  private int deletePlayer(CommandContext<ServerCommandSource> context, String playerIdStr) {
    try {
      UUID playerId = UUID.fromString(playerIdStr);
      boolean success = playerBao.deletePlayer(playerId);

      if (success) {
        context.getSource().sendFeedback(
                () -> Text.literal("§aDeleted player with ID: " + playerIdStr),
                false
        );
      } else {
        context.getSource().sendFeedback(
                () -> Text.literal("§cFailed to delete player with ID: " + playerIdStr),
                false
        );
      }
    } catch (IllegalArgumentException e) {
      context.getSource().sendFeedback(
              () -> Text.literal("§cInvalid UUID format: " + playerIdStr),
              false
      );
    }
    return 1;
  }

  /**
   * Lists all players in the database.
   *
   * @param context The command context
   * @return 1 on completion
   */
  private int listPlayers(CommandContext<ServerCommandSource> context) {
    List<Player> players = playerBao.listPlayers();

    if (players.isEmpty()) {
      context.getSource().sendFeedback(
              () -> Text.literal("§eNo players found in database"),
              false
      );
    } else {
      context.getSource().sendFeedback(
              () -> Text.literal("§aPlayers in database (" + players.size() + "):"),
              false
      );

      for (Player player : players) {
        context.getSource().sendFeedback(
                () -> Text.literal("§7- " + player.getUsername() + " §8(" + player.getId() + ")"),
                false
        );
      }
    }
    return 1;
  }
}