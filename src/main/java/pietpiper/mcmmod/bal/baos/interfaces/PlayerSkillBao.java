package pietpiper.mcmmod.bal.baos.interfaces;

import lombok.NonNull;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.persistence.dal.models.PlayerSkill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Player skill business access object interface. */
public interface PlayerSkillBao {

  /**
   * Initializes all {@link Skill} entries for a player.
   *
   * @param playerId The UUID of the player
   */
  void initializeSkills(@NonNull final UUID playerId);

  /**
   * Retrieves a specific {@link Skill} for a player.
   *
   * @param playerId The UUID of the player
   * @param skill The {@link Skill} to retrieve
   * @return An Optional containing the {@link PlayerSkill} if found
   */
  Optional<PlayerSkill> getSkill(@NonNull final UUID playerId,
                                 @NonNull final Skill skill);

  /**
   * Retrieves all skills for a player.
   *
   * @param playerId The UUID of the player
   * @return A list of {@link PlayerSkill}
   */
  List<PlayerSkill> listSkills(@NonNull final UUID playerId);

  /**
   * Updates a player's skill progression entry.
   *
   * @param playerSkill The {@link PlayerSkill} to update
   * @return true if update was successful, false otherwise
   */
  boolean updateSkill(@NonNull final PlayerSkill playerSkill);

  /**
   * Checks if a player has a specific skill entry.
   *
   * @param playerId The UUID of the player
   * @param skill The {@link Skill} to check
   * @return true if the skill entry exists
   */
  boolean skillExists(@NonNull final UUID playerId,
                      @NonNull final Skill skill);
}
