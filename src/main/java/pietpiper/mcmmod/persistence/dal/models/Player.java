package pietpiper.mcmmod.persistence.dal.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

/** Module representing a {@link Player}. */
@Builder
@Value
public class Player {
  @NonNull UUID id;
  @NonNull String username;

  // TODO: Add created_at & updated_at fields
}
