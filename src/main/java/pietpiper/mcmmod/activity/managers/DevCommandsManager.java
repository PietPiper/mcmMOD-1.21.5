package pietpiper.mcmmod.activity.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.context.CommandContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pietpiper.mcmmod.bal.PlayerIdentityBAL;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerBao;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerSkillBao;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.persistence.dal.models.Player;
import pietpiper.mcmmod.persistence.dal.models.PlayerSkill;

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
  private final PlayerSkillBao playerSkillBao;

  /**
   * Registers a new player in the system by resolving their Mojang UUID.
   *
   * @param context The command context
   * @param username The username to register
   * @return 1 on command execution
   */
  public int registerPlayer(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String username) {

    final MinecraftServer server = context.getSource().getServer();

    sendFeedback(context, "Resolving Mojang UUID for username: " + username + "...");

    playerIdentityBAL.resolveUuid(username)
            .thenAccept(uuidOptional ->
                    server.execute(() -> {

                      if (uuidOptional.isEmpty()) {
                        sendError(context, "Username not found: " + username);
                        return;
                      }

                      final UUID playerId = uuidOptional.get();

                      registerPlayerManager.registerPlayer(playerId, username);

                      sendSuccess(context,
                              "Registered player: " + username + " (" + playerId + ")");
                    }))
            .exceptionally(ex -> {
              server.execute(() ->
                      sendError(context,
                              "Failed to resolve Mojang UUID for username: " + username));
              return null;
            });

    return 1;
  }

  /**
   * Retrieves and displays player information.
   *
   * Accepts either a UUID or a username.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @return 1 on completion
   */
  public int getPlayer(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier) {

    return resolvePlayer(context, identifier, playerId ->
            playerBao.getPlayer(playerId).ifPresentOrElse(
                    player -> sendSuccess(context,
                            "Player found: " + player.getUsername() + " (" + player.getId() + ")"),
                    () -> sendError(context, "Player not found: " + identifier)
            ));
  }

  /**
   * Updates a player's username.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @param newUsername The new username
   * @return 1 on completion
   */
  public int updatePlayer(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier,
          @NonNull final String newUsername) {

    return resolvePlayer(context, identifier, playerId -> {

      final boolean success = playerBao.updatePlayerUsername(playerId, newUsername);

      if (success) {
        sendSuccess(context, "Updated player to: " + newUsername);
      } else {
        sendError(context, "Failed to update player");
      }

    });
  }

  /**
   * Deletes a player from the system.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @return 1 on completion
   */
  public int deletePlayer(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier) {

    return resolvePlayer(context, identifier, playerId -> {

      final boolean success = playerBao.deletePlayer(playerId);

      if (success) {
        sendSuccess(context, "Deleted player: " + playerId);
      } else {
        sendError(context, "Failed to delete player");
      }

    });
  }

  /**
   * Lists all players currently stored in the database.
   *
   * @param context The command context
   * @return 1 on completion
   */
  public int listPlayers(@NonNull final CommandContext<ServerCommandSource> context) {

    final List<Player> players = playerBao.listPlayers();

    if (players.isEmpty()) {
      sendWarning(context, "No players found in database");
      return 1;
    }

    sendSuccess(context, "Players in database (" + players.size() + "):");

    players.forEach(player ->
            sendFeedback(context,
                    "§7- " + player.getUsername() + " §8(" + player.getId() + ")"));

    return 1;
  }

  /**
   * Initializes all skills for a player.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @return 1 on completion
   */
  public int initializeSkills(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier) {

    return resolvePlayer(context, identifier, playerId -> {
      playerSkillBao.initializeSkills(playerId);
      sendSuccess(context, "Initialized skills for player: " + playerId);
    });
  }

  /**
   * Retrieves a specific skill for a player.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @param skillStr The skill name
   * @return 1 on completion
   */
  public int getSkill(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier,
          @NonNull final String skillStr) {

    return resolvePlayer(context, identifier, playerId -> {

      try {

        final Skill skill = Skill.valueOf(skillStr.toUpperCase());

        playerSkillBao.getSkill(playerId, skill)
                .ifPresentOrElse(
                        skillData -> sendSuccess(context,
                                skill + " -> Level=" + skillData.getLevel() +
                                        " XP=" + skillData.getXp()),
                        () -> sendError(context, "Skill not found"));

      } catch (IllegalArgumentException e) {
        sendError(context, "Invalid skill: " + skillStr);
      }

    });
  }

  /**
   * Lists all skills belonging to a player.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @return 1 on completion
   */
  public int listSkills(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier) {

    return resolvePlayer(context, identifier, playerId -> {

      final List<PlayerSkill> skills = playerSkillBao.listSkills(playerId);

      if (skills.isEmpty()) {
        sendWarning(context, "No skills found for player");
        return;
      }

      sendSuccess(context, "Skills for player " + playerId + ":");

      skills.forEach(skill ->
              sendFeedback(context,
                      "§7- " + skill.getSkill() +
                              " §8(Level=" + skill.getLevel() +
                              ", XP=" + skill.getXp() + ")"));

    });
  }

  /**
   * Sets the level and experience for a player's skill.
   *
   * @param context The command context
   * @param identifier UUID or username
   * @param skillStr Skill name
   * @param level Level value
   * @param xp Experience value
   * @return 1 on completion
   */
  public int setSkill(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier,
          @NonNull final String skillStr,
          final int level,
          final long xp) {

    return resolvePlayer(context, identifier, playerId -> {

      try {

        final Skill skill = Skill.valueOf(skillStr.toUpperCase());

        final Optional<PlayerSkill> skillOpt = playerSkillBao.getSkill(playerId, skill);

        if (skillOpt.isEmpty()) {
          sendError(context, "Skill does not exist for player");
          return;
        }

        final PlayerSkill updated =
                skillOpt.get()
                        .withLevel(level)
                        .withXp(xp);

        final boolean success = playerSkillBao.updateSkill(updated);

        if (success) {
          sendSuccess(context,
                  "Updated skill " + skill +
                          " -> Level=" + level +
                          " XP=" + xp);
        } else {
          sendError(context, "Failed to update skill");
        }

      } catch (IllegalArgumentException e) {
        sendError(context, "Invalid skill: " + skillStr);
      }

    });
  }

  /**
   * Resolves a player identifier into a UUID.
   * <p>
   * Accepts either a UUID or a username. When resolving usernames,
   * this method verifies that the database username matches the
   * Mojang resolved username to avoid identity ambiguity.
   *
   * @param context Command context
   * @param identifier Username or UUID
   * @param action Action to execute once resolved
   * @return 1 on completion
   */
  private int resolvePlayer(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String identifier,
          @NonNull final Consumer<UUID> action) {

    final MinecraftServer server = context.getSource().getServer();

    try {
      final UUID playerId = UUID.fromString(identifier);
      action.accept(playerId);
      return 1;
    }
    catch (IllegalArgumentException ignored) {}

    sendFeedback(context, "Resolving Mojang UUID for username: " + identifier + "...");

    playerIdentityBAL.resolveUuid(identifier)
            .thenAccept(uuidOptional ->
                    server.execute(() -> {

                      if (uuidOptional.isEmpty()) {
                        sendError(context, "Username not found: " + identifier);
                        return;
                      }

                      final UUID resolvedId = uuidOptional.get();
                      final Optional<Player> dbPlayer = playerBao.getPlayer(resolvedId);

                      if (dbPlayer.isPresent()) {

                        final Player player = dbPlayer.get();

                        if (!player.getUsername().equalsIgnoreCase(identifier)) {

                          sendError(context, "Player identity ambiguity detected.");

                          sendFeedback(context,
                                  "Resolved Mojang username: "
                                          + identifier + " -> " + resolvedId);

                          sendFeedback(context,
                                  "Database record: "
                                          + player.getUsername()
                                          + " -> "
                                          + player.getId());

                          sendWarning(context,
                                  "Please run the command again using the UUID.");

                          return;
                        }
                      }

                      action.accept(resolvedId);

                    }))
            .exceptionally(ex -> {
              server.execute(() ->
                      sendError(context,
                              "Failed to resolve Mojang UUID for username: " + identifier));
              return null;
            });

    return 1;
  }

  private void sendFeedback(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String message) {

    context.getSource().sendFeedback(() -> Text.literal(message), false);
  }

  private void sendSuccess(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String message) {

    sendFeedback(context, "§a" + message);
  }

  private void sendError(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String message) {

    sendFeedback(context, "§c" + message);
  }

  private void sendWarning(
          @NonNull final CommandContext<ServerCommandSource> context,
          @NonNull final String message) {

    sendFeedback(context, "§e" + message);
  }
}
