package pietpiper.mcmmod.persistence.dal.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import pietpiper.mcmmod.config.skill.Skill;

import javax.annotation.Nullable;
import java.util.UUID;

/** Model representing a Player's Skill progression. */
@Builder
@Value
@With
public class PlayerSkill {

  @NonNull UUID playerId;
  @NonNull Skill skill;

  int level;
  long xp;

  @Nullable String metadata;
}
