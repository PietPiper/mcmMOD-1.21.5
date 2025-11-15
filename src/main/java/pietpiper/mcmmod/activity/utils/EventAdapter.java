package pietpiper.mcmmod.activity.utils;

import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import pietpiper.mcmmod.activity.requests.JoinEventRequest;

import java.util.function.Consumer;

/** Adapter for converting Fabric events to request objects. */
@UtilityClass
public class EventAdapter {

  /**
   * Adapts a JOIN event consumer to the raw event parameters.
   *
   * @param consumer the {@link Consumer} that processes the {@link JoinEventRequest}
   * @return a {@link ServerPlayConnectionEvents.Join} event handler
   */
  public static ServerPlayConnectionEvents.Join adaptJoinEvent(Consumer<JoinEventRequest> consumer) {
    return (handler, sender, server) ->
            consumer.accept(JoinEventRequest.of(handler, sender, server));
  }
}