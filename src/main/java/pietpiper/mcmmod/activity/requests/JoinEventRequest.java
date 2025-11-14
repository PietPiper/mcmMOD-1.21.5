package pietpiper.mcmmod.activity.requests;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.UUID;

/** Request object representing a player join event. */
@Value
@Builder
public class JoinEventRequest {
  @NonNull ServerPlayNetworkHandler handler;
  @NonNull PacketSender sender;
  @NonNull MinecraftServer server;

  /**
   * Creates a {@link JoinEventRequest} from the player join event parameters.
   *
   * @param handler the {@link ServerPlayNetworkHandler} for the joining player
   * @param sender the {@link PacketSender} for the connection
   * @param server the {@link MinecraftServer} instance
   * @return a new {@link JoinEventRequest} containing the event data
   */
  public static JoinEventRequest of(
          @NonNull final ServerPlayNetworkHandler handler,
          @NonNull final PacketSender sender,
          @NonNull final MinecraftServer server) {
    return JoinEventRequest.builder()
            .handler(handler)
            .sender(sender)
            .server(server)
            .build();
  }

  /** Get player UUID. */
  public UUID getPlayerId() {
    return handler.getPlayer().getUuid();
  }

  /** Get username. */
  public String getUsername() {
    return handler.getPlayer().getName().getString();
  }
}