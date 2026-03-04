package pietpiper.mcmmod.bal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.client.mojang.MojangClient;

import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerIdentityBAL {

  private final MojangClient mojangClient;

  /**
   * Resolves a Minecraft username to its permanent UUID.
   *
   * @param username The Minecraft username
   * @return Optional UUID if the username exists
   */
  public Optional<UUID> resolveUuid(@NonNull final String username) {
    return mojangClient.resolveUuid(username);
  }
}
