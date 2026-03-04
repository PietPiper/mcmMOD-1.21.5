package pietpiper.mcmmod.client.mojang;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MojangClient {

  private static final String MOJANG_PROFILE_URL =
          "https://api.mojang.com/users/profiles/minecraft/";

  private final HttpClient httpClient;

  /**
   * Resolves a Minecraft username to a UUID using Mojang's public API.
   *
   * @param username The Minecraft username
   * @return Optional UUID if found, empty otherwise
   */
  public Optional<UUID> resolveUuid(@NonNull final String username) {

    try {
      final HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(MOJANG_PROFILE_URL + username))
              .GET()
              .build();

      final HttpResponse<String> response =
              httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 204) {
        return Optional.empty();
      }

      if (response.statusCode() != 200) {
        return Optional.empty();
      }

      final String body = response.body();

      // {"id":"069a79f444e94726a5befca90e38aaf5","name":"Notch"}
      String rawId = body.split("\"id\":\"")[1].split("\"")[0];

      return Optional.of(formatUuid(rawId));

    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    }
  }

  private UUID formatUuid(@NonNull final String raw) {
    final String formatted =
            raw.substring(0, 8) + "-" +
                    raw.substring(8, 12) + "-" +
                    raw.substring(12, 16) + "-" +
                    raw.substring(16, 20) + "-" +
                    raw.substring(20);

    return UUID.fromString(formatted);
  }
}
