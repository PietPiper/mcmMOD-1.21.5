package pietpiper.mcmmod.activity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.activity.managers.RegisterPlayerManager;
import pietpiper.mcmmod.activity.requests.JoinEventRequest;

import static pietpiper.mcmmod.McmMod.log;

/** Activity for handling player registration workflow. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RegisterPlayerActivity {
  private final RegisterPlayerManager registerPlayerManager;

  /**
   * Executes the player registration activity.
   *
   * @param request The {@link JoinEventRequest}
   */
  public void execute(@NonNull final JoinEventRequest request) {
    if (request.getUsername().isBlank()) {
      log.error("Player cannot have a blank username");
      return;
    }

    registerPlayerManager.registerPlayer(request.getPlayerId(), request.getUsername());
  }
}