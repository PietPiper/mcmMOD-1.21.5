package pietpiper.mcmmod.clients.models;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Response from Mojang player endpoint. */
@Value
@Builder
@Jacksonized
public class MojangProfileResponse {
  String id;
  String name;
}
