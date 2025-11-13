package pietpiper.mcmmod.persistence.dal.models;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/** Module representing a {@link Player}. */
@Builder
@Value
public class Player {
  UUID id;
  String username;
}
