package pietpiper.mcmmod.persistence.dal.daos;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import pietpiper.mcmmod.config.skill.Skill;
import pietpiper.mcmmod.persistence.dal.daos.interfaces.PlayerSkillDao;
import pietpiper.mcmmod.persistence.dal.models.PlayerSkill;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pietpiper.mcmmod.McmMod.log;

/** Implementation of PlayerSkillDao. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerSkillDaoImpl implements PlayerSkillDao {

  private final QueryRunner queryRunner;

  private static final String INSERT_SKILL_SQL =
          """
          INSERT OR IGNORE INTO player_skills
          (player_id, skill, level, xp, metadata)
          VALUES (?, ?, ?, ?, ?)
          """;

  private static final String SELECT_SKILL_SQL =
          """
          SELECT player_id, skill, level, xp, metadata
          FROM player_skills
          WHERE player_id = ? AND skill = ?
          """;

  private static final String SELECT_ALL_SKILLS_SQL =
          """
          SELECT player_id, skill, level, xp, metadata
          FROM player_skills
          WHERE player_id = ?
          """;

  private static final String UPDATE_SKILL_SQL =
          """
          UPDATE player_skills
          SET level = ?, xp = ?, metadata = ?
          WHERE player_id = ? AND skill = ?
          """;

  private static final String DELETE_SKILLS_SQL =
          """
          DELETE FROM player_skills WHERE player_id = ?
          """;

  @Override
  public void initializeSkillsForPlayer(@NonNull final UUID playerId) {
    try {
      for (Skill skill : Skill.values()) {
        queryRunner.update(
                INSERT_SKILL_SQL,
                playerId.toString(),
                skill.name(),
                1,
                0L,
                null
        );
      }

      log.info("Initialized skills for player {}", playerId);

    } catch (SQLException e) {
      log.error("Failed to initialize skills for player {}", playerId, e);
    }
  }

  @Override
  public Optional<PlayerSkill> getSkill(@NonNull final UUID playerId,
                                        @NonNull final Skill skill) {
    try {
      final PlayerSkill result = queryRunner.query(
              SELECT_SKILL_SQL,
              singleSkillHandler,
              playerId.toString(),
              skill.name()
      );

      return Optional.ofNullable(result);

    } catch (SQLException e) {
      log.error("Failed to fetch skill {} for player {}",
              skill, playerId, e);
      return Optional.empty();
    }
  }

  @Override
  public List<PlayerSkill> listSkills(@NonNull final UUID playerId) {
    try {
      final List<PlayerSkill> skills = queryRunner.query(
              SELECT_ALL_SKILLS_SQL,
              skillListHandler,
              playerId.toString()
      );

      return skills != null
              ? skills
              : new ArrayList<>();

    } catch (SQLException e) {
      log.error("Failed to list skills for player {}", playerId, e);
      return new ArrayList<>();
    }
  }

  @Override
  public void updateSkill(@NonNull final PlayerSkill playerSkill) {
    try {
      queryRunner.update(
              UPDATE_SKILL_SQL,
              playerSkill.getLevel(),
              playerSkill.getXp(),
              playerSkill.getMetadata(),
              playerSkill.getPlayerId().toString(),
              playerSkill.getSkill().name()
      );

    } catch (SQLException e) {
      log.error("Failed to update skill {} for player {}",
              playerSkill.getSkill(),
              playerSkill.getPlayerId(),
              e);
    }
  }

  @Override
  public void deleteSkills(@NonNull final UUID playerId) {
    try {
      queryRunner.update(
              DELETE_SKILLS_SQL,
              playerId.toString()
      );
    } catch (SQLException e) {
      log.error("Failed to delete skills for player {}", playerId, e);
    }
  }

  private final ResultSetHandler<PlayerSkill> singleSkillHandler =
          (ResultSet rs) -> {
            if (rs.next()) {
              return mapRow(rs);
            }
            return null;
          };

  private final ResultSetHandler<List<PlayerSkill>> skillListHandler =
          (ResultSet rs) -> {
            List<PlayerSkill> skills = new ArrayList<>();
            while (rs.next()) {
              skills.add(mapRow(rs));
            }
            return skills;
          };

  private PlayerSkill mapRow(final ResultSet rs) throws SQLException {
    return PlayerSkill.builder()
            .playerId(UUID.fromString(rs.getString("player_id")))
            .skill(Skill.valueOf(rs.getString("skill")))
            .level(rs.getInt("level"))
            .xp(rs.getLong("xp"))
            .metadata(rs.getString("metadata"))
            .build();
  }
}
