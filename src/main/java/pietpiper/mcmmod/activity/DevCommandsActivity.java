package pietpiper.mcmmod.activity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.command.ServerCommandSource;
import pietpiper.mcmmod.activity.managers.DevCommandsManager;
import pietpiper.mcmmod.activity.requests.CommandRegistrationRequest;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static pietpiper.mcmmod.McmMod.log;

/** Activity for handling developer command registration workflow. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DevCommandsActivity {

  private final DevCommandsManager devCommandsManager;

  /**
   * Executes the command registration workflow.
   *
   * @param request The command registration request
   */
  public void execute(@NonNull final CommandRegistrationRequest request) {
    register(request.getDispatcher());
    log.info("Registered dev commands");
  }

  /**
   * Registers all developer/admin commands with the command dispatcher.
   *
   * @param dispatcher The command dispatcher to register commands with
   */
  private void register(@NonNull final CommandDispatcher<ServerCommandSource> dispatcher) {
    final LiteralArgumentBuilder<ServerCommandSource> adminCommand = buildAdminCommand();
    dispatcher.register(adminCommand);
  }

  /**
   * Builds the main admin command with all subcommands.
   *
   * @return The built admin command
   */
  private LiteralArgumentBuilder<ServerCommandSource> buildAdminCommand() {

    return literal("admin")
            .requires(source -> source.hasPermissionLevel(2))

            .then(literal("player")
                    .then(buildRegisterCommand())
                    .then(buildGetCommand())
                    .then(buildUpdateCommand())
                    .then(buildDeleteCommand())
                    .then(buildListCommand()))

            .then(literal("skill")
                    .then(buildInitSkillsCommand())
                    .then(buildGetSkillCommand())
                    .then(buildListSkillsCommand())
                    .then(buildSetSkillCommand()));
  }

  /** Builds register command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildRegisterCommand() {
    return literal("register")
            .then(argument("username", StringArgumentType.string())
                    .executes(context ->
                            devCommandsManager.registerPlayer(
                                    context,
                                    StringArgumentType.getString(context, "username"))));
  }

  /** Builds get player command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildGetCommand() {
    return literal("get")
            .then(argument("identifier", StringArgumentType.string())
                    .executes(context ->
                            devCommandsManager.getPlayer(
                                    context,
                                    StringArgumentType.getString(context, "identifier"))));
  }

  /** Builds update player command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildUpdateCommand() {
    return literal("update")
            .then(argument("identifier", StringArgumentType.string())
                    .then(argument("newUsername", StringArgumentType.string())
                            .executes(context ->
                                    devCommandsManager.updatePlayer(
                                            context,
                                            StringArgumentType.getString(context, "identifier"),
                                            StringArgumentType.getString(context, "newUsername")))));
  }

  /** Builds delete player command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildDeleteCommand() {
    return literal("delete")
            .then(argument("identifier", StringArgumentType.string())
                    .executes(context ->
                            devCommandsManager.deletePlayer(
                                    context,
                                    StringArgumentType.getString(context, "identifier"))));
  }

  /** Builds list players command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildListCommand() {
    return literal("list")
            .executes(devCommandsManager::listPlayers);
  }

  /** Builds skill init command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildInitSkillsCommand() {
    return literal("init")
            .then(argument("identifier", StringArgumentType.string())
                    .executes(context ->
                            devCommandsManager.initializeSkills(
                                    context,
                                    StringArgumentType.getString(context, "identifier"))));
  }

  /** Builds skill get command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildGetSkillCommand() {
    return literal("get")
            .then(argument("identifier", StringArgumentType.string())
                    .then(argument("skill", StringArgumentType.string())
                            .executes(context ->
                                    devCommandsManager.getSkill(
                                            context,
                                            StringArgumentType.getString(context, "identifier"),
                                            StringArgumentType.getString(context, "skill")))));
  }

  /** Builds skill list command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildListSkillsCommand() {
    return literal("list")
            .then(argument("identifier", StringArgumentType.string())
                    .executes(context ->
                            devCommandsManager.listSkills(
                                    context,
                                    StringArgumentType.getString(context, "identifier"))));
  }

  /** Builds skill set command. */
  private LiteralArgumentBuilder<ServerCommandSource> buildSetSkillCommand() {
    return literal("set")
            .then(argument("identifier", StringArgumentType.string())
                    .then(argument("skill", StringArgumentType.string())
                            .then(argument("level", IntegerArgumentType.integer())
                                    .then(argument("xp", LongArgumentType.longArg())
                                            .executes(context ->
                                                    devCommandsManager.setSkill(
                                                            context,
                                                            StringArgumentType.getString(context, "identifier"),
                                                            StringArgumentType.getString(context, "skill"),
                                                            IntegerArgumentType.getInteger(context, "level"),
                                                            LongArgumentType.getLong(context, "xp")))))));
  }
}
