package pietpiper.mcmmod.bal.baos;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.bal.baos.interfaces.PlayerSkillBao;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerSkillDao;
import pietpiper.mcmmod.persistence.dal.models.PlayerSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pietpiper.mcmmod.McmMod.log;

/** Implementation for a {@link PlayerSkillBao}. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerSkillBaoImpl implements PlayerSkillBao {

  private final PlayerSkillDao playerSkillDao;

  @Override
  public void initializeSkills(@NonNull final UUID playerId) {
    try {
      playerSkillDao.initializeSkillsForPlayer(playerId);
    } catch (Exception e) {
      log.error("Failed to initialize skills for player: {}", playerId, e);
    }
  }

  @Override
  public Optional<PlayerSkill> getSkill(@NonNull final UUID playerId,
                                        @NonNull final Skill skill) {
    try {
      return playerSkillDao.getSkill(playerId, skill);
    } catch (Exception e) {
      log.error("Error retrieving skill {} for player {}",
              skill, playerId, e);
      return Optional.empty();
    }
  }

  @Override
  public List<PlayerSkill> listSkills(@NonNull final UUID playerId) {
    try {
      return playerSkillDao.listSkills(playerId);
    } catch (Exception e) {
      log.error("Error retrieving skills for player {}", playerId, e);
      return new ArrayList<>();
    }
  }

  @Override
  public boolean updateSkill(@NonNull final PlayerSkill playerSkill) {
    try {
      playerSkillDao.updateSkill(playerSkill);
      return true;
    } catch (Exception e) {
      log.error("Failed to update skill {} for player {}",
              playerSkill.getSkill(),
              playerSkill.getPlayerId(),
              e);
      return false;
    }
  }

  @Override
  public boolean skillExists(@NonNull final UUID playerId,
                             @NonNull final Skill skill) {
    try {
      return playerSkillDao.getSkill(playerId, skill).isPresent();
    } catch (Exception e) {
      log.error("Error checking skill existence for player {} and skill {}",
              playerId, skill, e);
      return false;
    }
  }
}
