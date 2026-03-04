package pietpiper.mcmmod.bal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.client.mojang.MojangClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Business logic for determining a player's identity. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerIdentityBAL {

  private final MojangClient mojangClient;
  private final Clock clock;

  // TODO: Move cache out of this file
  private final Map<String, CacheEntry> uuidCache = new ConcurrentHashMap<>();
  private static final Duration CACHE_TTL = Duration.ofMinutes(30);

  /**
   * Resolves a Minecraft username to its permanent UUID.
   *
   * @param username The Minecraft username
   * @return {@link CompletableFuture} containing Optional UUID
   */
  public CompletableFuture<Optional<UUID>> resolveUuid(@NonNull final String username) {

    final String normalized = username.toLowerCase();
    final Instant now = clock.instant();

    final CacheEntry cached = uuidCache.get(normalized);

    if (cached != null && !cached.isExpired(now)) {
      return CompletableFuture.completedFuture(cached.result);    }

    return mojangClient.resolveUuid(username)
            .thenApply(result -> {

              uuidCache.put(
                      normalized,
                      new CacheEntry(result, now.plus(CACHE_TTL)));

              return result;
            });
  }

  /**
   * Simple cache entry used to store UUID lookup results with expiration.
   */
  @RequiredArgsConstructor
  private static class CacheEntry {

    final Optional<UUID> result;
    final Instant expiresAt;

    boolean isExpired(@NonNull final Instant now) {
      return now.isAfter(expiresAt);
    }
  }
}
