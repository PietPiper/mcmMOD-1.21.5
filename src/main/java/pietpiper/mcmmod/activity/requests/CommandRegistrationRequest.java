package pietpiper.mcmmod.activity.requests;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/** Request object representing a command registration event. */
@Value
@Builder
public class CommandRegistrationRequest {
  @NonNull CommandDispatcher<ServerCommandSource> dispatcher;

  /**
   * Creates a {@link CommandRegistrationRequest} from the command registration event parameters.
   *
   * @param dispatcher the {@link CommandDispatcher} for registering commands
   * @return a new {@link CommandRegistrationRequest} containing the command dispatcher
   */
  public static CommandRegistrationRequest of(@NonNull final CommandDispatcher<ServerCommandSource> dispatcher) {
    return CommandRegistrationRequest.builder()
            .dispatcher(dispatcher)
            .build();
  }
}