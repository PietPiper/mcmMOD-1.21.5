package pietpiper.mcmmod.activity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pietpiper.mcmmod.activity.commands.DevCommands;
import pietpiper.mcmmod.activity.requests.CommandRegistrationRequest;

import static pietpiper.mcmmod.McmMod.log;

/** Activity for handling command registration workflow. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DevCommandsActivity {

  /**
   * Executes the command registration workflow.
   *
   * @param request The command registration request
   */
  public void execute(@NonNull CommandRegistrationRequest request) {
    DevCommands.register(request.getDispatcher());
    log.info("Registered commands");
  }
}