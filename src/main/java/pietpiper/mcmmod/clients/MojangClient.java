package pietpiper.mcmmod.client.mojang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.clients.models.MojangProfileResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static pietpiper.mcmmod.McmMod.log;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MojangClient {

  private static final String MOJANG_PROFILE_URL =
          "https://api.mojang.com/users/profiles/minecraft/";

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper;

  /**
   *  Resolves a Minecraft username to a UUID.
   *
   * @param username The Minecraft username
   * @return {@link CompletableFuture} containing Optional UUID
   */
  public CompletableFuture<Optional<UUID>> resolveUuid(@NonNull String username) {

    log.debug("Resolving UUID for username: {}", username);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MOJANG_PROFILE_URL + username))
            .GET()
            .build();

    return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {

              final int status = response.statusCode();
              final String body = response.body();

              log.debug("Mojang API response for {}: status={}, body={}",
                      username, status, body);

              if (status == 204) {
                log.debug("Username not found: {}", username);
                return Optional.<UUID>empty();
              }

              if (status != 200) {
                log.warn("Unexpected Mojang API status for {}: {}", username, status);
                return Optional.<UUID>empty();
              }

              try {

                final MojangProfileResponse profile =
                        objectMapper.readValue(body, MojangProfileResponse.class);

                final UUID uuid = formatUuid(profile.getId());

                log.debug("Resolved Mojang UUID for {} -> {}", username, uuid);

                return Optional.of(uuid);

              } catch (Exception e) {
                log.error("Failed to parse Mojang response for {}: {}", username, body, e);
                return Optional.<UUID>empty();
              }
            })
            .exceptionally(ex -> {
              log.error("Mojang API request failed for username: {}", username, ex);
              return Optional.<UUID>empty();
            });
  }

  private UUID formatUuid(String raw) {

    String formatted =
            raw.substring(0, 8) + "-" +
                    raw.substring(8, 12) + "-" +
                    raw.substring(12, 16) + "-" +
                    raw.substring(16, 20) + "-" +
                    raw.substring(20);

    return UUID.fromString(formatted);
  }
}
