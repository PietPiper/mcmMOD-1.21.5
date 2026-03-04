package pietpiper.mcmmod.bal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.client.mojang.MojangClient;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Businless logic for determining a players identity. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerIdentityBAL {

  private final MojangClient mojangClient;

  /**
   * Resolves a Minecraft username to its permanent UUID.
   *
   * @param username The Minecraft username
   * @return {@link CompletableFuture} containing Optional UUID
   */
  public CompletableFuture<Optional<UUID>> resolveUuid(@NonNull final String username) {
    return mojangClient.resolveUuid(username);
  }
}
