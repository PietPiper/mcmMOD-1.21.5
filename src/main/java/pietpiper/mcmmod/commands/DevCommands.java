package pietpiper.mcmmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class DevCommands {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(literal("ping")
            .executes(DevCommands::pong)
    );
  }

  private static int pong(CommandContext<ServerCommandSource> context) {
    context.getSource().sendFeedback(
            () -> Text.literal("§aPong!"),
            false
    );
    return 1;
  }
}