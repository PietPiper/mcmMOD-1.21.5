package pietpiper.mcmmod.persistence.dal.daos.interfaces;

import lombok.NonNull;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.persistence.dal.models.PlayerSkill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Player skill data access object interface. */
public interface PlayerSkillDao {

  /**
   * Initializes all {@link Skill} entries for a player.
   * <p>
   * This eagerly inserts a row for every {@link Skill} defined in the system
   * with default level, xp, and metadata values.
   *
   * @param playerId The UUID of the player whose skills should be initialized.
   */
  void initializeSkillsForPlayer(@NonNull final UUID playerId);

  /**
   * Retrieves a specific {@link Skill} progression entry for a player.
   *
   * @param playerId The UUID of the player.
   * @param skill The {@link Skill} to retrieve.
   * @return An Optional containing the {@link PlayerSkill} if found, empty otherwise.
   */
  Optional<PlayerSkill> getSkill(@NonNull final UUID playerId,
                                 @NonNull final Skill skill);

  /**
   * Retrieves all {@link Skill} progression entries for a player.
   *
   * @param playerId The UUID of the player.
   * @return A list of all {@link PlayerSkill} entries for the player.
   */
  List<PlayerSkill> listSkills(@NonNull final UUID playerId);

  /**
   * Updates an existing {@link PlayerSkill} entry.
    <p>
   * This updates level, xp, and metadata for the specified player skill.
   *
   * @param playerSkill The {@link PlayerSkill} to update.
   */
  void updateSkill(@NonNull final PlayerSkill playerSkill);

  /**
   * Deletes all skill progression entries for a player.
   *
   * @param playerId The UUID of the player whose skills should be removed.
   */
  void deleteSkills(@NonNull final UUID playerId);
}
