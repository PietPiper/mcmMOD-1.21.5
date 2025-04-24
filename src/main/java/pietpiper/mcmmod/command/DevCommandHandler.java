package pietpiper.mcmmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import pietpiper.mcmmod.data.PlayerDataManager;
import pietpiper.mcmmod.data.XPUtil;
import pietpiper.mcmmod.skill.Skill;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DevCommandHandler {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mcmmod")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("getXp")
                        .then(argument("skill", StringArgumentType.word())
                                .executes(ctx -> getXP(ctx, null))
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> getXP(ctx, StringArgumentType.getString(ctx, "player"))))))
                .then(literal("getRemainingXp")
                        .then(argument("skill", StringArgumentType.word())
                                .executes(ctx -> getRemainingXP(ctx, null))
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> getRemainingXP(ctx, StringArgumentType.getString(ctx, "player"))))))
                .then(literal("getLevel")
                        .then(argument("skill", StringArgumentType.word())
                                .executes(ctx -> getLevel(ctx, null))
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> getLevel(ctx, StringArgumentType.getString(ctx, "player"))))))
                .then(literal("setLevel")
                        .then(argument("skill", StringArgumentType.word())
                                .then(argument("level", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setLevel(ctx, null))
                                        .then(argument("player", StringArgumentType.word())
                                                .executes(ctx -> setLevel(ctx, StringArgumentType.getString(ctx, "player")))))))
                .then(literal("setXp")
                        .then(argument("skill", StringArgumentType.word())
                                .then(argument("xp", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setXP(ctx, null))
                                        .then(argument("player", StringArgumentType.word())
                                                .executes(ctx -> setXP(ctx, StringArgumentType.getString(ctx, "player")))))))
                .then(literal("setRemainingXp")
                        .then(argument("skill", StringArgumentType.word())
                                .then(argument("xp", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setRemainingXP(ctx, null))
                                        .then(argument("player", StringArgumentType.word())
                                                .executes(ctx -> setRemainingXP(ctx, StringArgumentType.getString(ctx, "player")))))))
                .then(literal("addXp")
                        .then(argument("skill", StringArgumentType.word())
                                .then(argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> addXP(ctx, null))
                                        .then(argument("player", StringArgumentType.word())
                                                .executes(ctx -> addXP(ctx, StringArgumentType.getString(ctx, "player")))))))
                .then(literal("addLevels")
                        .then(argument("skill", StringArgumentType.word())
                                .then(argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> addLevels(ctx, null))
                                        .then(argument("player", StringArgumentType.word())
                                                .executes(ctx -> addLevels(ctx, StringArgumentType.getString(ctx, "player")))))))
                .then(literal("lifetimeXP")
                        .then(argument("skill", StringArgumentType.word())
                                .executes(ctx -> getLifetimeXP(ctx, null))
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> getLifetimeXP(ctx, StringArgumentType.getString(ctx, "player"))))))
                .then(literal("xpForLevel")
                        .then(argument("level", IntegerArgumentType.integer())
                                .executes(DevCommandHandler::getXPtoLevelUp)))
                .then(literal("xpToReachLevel")
                        .then(argument("level", IntegerArgumentType.integer())
                                .executes(DevCommandHandler::getXPToReachLvl)))
                .then(literal("resetPlayerData")
                        .then(argument("player", StringArgumentType.word())
                                .executes(ctx -> resetPlayerSkills(ctx, StringArgumentType.getString(ctx, "player")))))
        );
    }

    //Prints the amount of experience the target player currently has towards their next level up in the specified skill.
    private static int getXP(CommandContext<ServerCommandSource> ctx, String targetName) throws CommandSyntaxException {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        int xp = PlayerDataManager.getXP(player.getUuid(), skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " has " + xp + " XP in " + skill.getDisplayName() + "."),
                false
        );
        return 1;
    }

    //Prints the amount of experience the target player requires to reach the next level in the specified skill.
    private static int getRemainingXP(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        int remainingXP = PlayerDataManager.getRemainingXP(player.getUuid(), skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " has " + remainingXP + " XP remaining required to level up in " + skill.getDisplayName() + "."),
                false
        );
        return 1;
    }

    //Prints the target players level in the specified skill.
    private static int getLevel(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        int xp = PlayerDataManager.getLevel(player.getUuid(), skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " has " + xp + " Levels in " + skill.getDisplayName() + "."),
                false
        );
        return 1;
    }

    //Sets the target players level in the specified skill to the specified value.
    private static int setLevel(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        int levelToSet = IntegerArgumentType.getInteger(ctx, "level");
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        PlayerDataManager.setLevel(player.getUuid(), levelToSet, skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " " + skill.getDisplayName() + " level has been set to " + levelToSet + "."),
                false
        );
        return 1;
    }

    //Sets the amount of xp that the target player has accumulated towards their next level in the specified skill to the specified value.
    private static int setXP(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        int xpToSet = IntegerArgumentType.getInteger(ctx, "xp");
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        PlayerDataManager.setLevel(player.getUuid(), xpToSet, skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " " + skill.getDisplayName() + " XP has been set to " + xpToSet + "."),
                false
        );
        return 1;
    }

    //Sets the required remaining experience to reach the next level up for the target player in the specified skill to the specified value.
    private static int setRemainingXP(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        int remainingXPToSet = IntegerArgumentType.getInteger(ctx, "xp");
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        PlayerDataManager.setLevel(player.getUuid(), remainingXPToSet, skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " " + skill.getDisplayName() + " remaining XP has been set to " + remainingXPToSet + "."),
                false
        );
        return 1;
    }

    //Adds the specified amount of experience to the target player in the specified skill.
    private static int addXP(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        int xpToAdd = IntegerArgumentType.getInteger(ctx, "amount");
        Skill skill = Skill.fromName(skillName);
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        XPUtil.addXP(player.getUuid(), skill, xpToAdd);
        ctx.getSource().sendFeedback(
                () -> Text.literal(xpToAdd + " XP has been added to " + player.getName().getString() + "'s " + skill.getDisplayName() + " skill."),
                false
        );
        return 1;
    }

    //Adds the specified amount of levels to the target players level in the specified skill.
    private static int addLevels(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        int lvlsToAdd = IntegerArgumentType.getInteger(ctx, "amount");
        Skill skill = Skill.fromName(skillName);
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        int curLvl = PlayerDataManager.getLevel(player.getUuid(), skill);
        PlayerDataManager.setLevel(player.getUuid(), lvlsToAdd, skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(lvlsToAdd + " levels have been added to " + player.getName().getString() + "'s " + skill.getDisplayName() + " skill."),
                false
        );
        return 1;
    }

    //Prints the amount of xp to reach the next level at the target level assuming you have zero experience towards your next level up.
    private static int getXPtoLevelUp(CommandContext<ServerCommandSource> ctx) {
        int lvl = IntegerArgumentType.getInteger(ctx, "level");

        int lvlXP = XPUtil.getXPRequiredForLevelUp(lvl);

        ctx.getSource().sendFeedback(
                () -> Text.literal("A player needs " + lvlXP + " XP total at level " + lvl + " to reach level " + (lvl + 1) + " in a skill."),
                false
        );
        return 1;

    }

    //Prints the total amount of experience the player has obtained in a skill.
    private static int getLifetimeXP(CommandContext<ServerCommandSource> ctx, String targetName) {
        String skillName = StringArgumentType.getString(ctx, "skill");
        Skill skill = Skill.fromName(skillName);
        if (skill == null) {
            ctx.getSource().sendError(Text.literal("Invalid skill: " + skillName));
            return 0;
        }

        final ServerPlayerEntity player = (targetName == null)
                ? ctx.getSource().getPlayer()
                : ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found."));
            return 0;
        }

        int lifetimeXP = XPUtil.getLifetimeXP(player.getUuid(), skill);
        ctx.getSource().sendFeedback(
                () -> Text.literal(player.getName().getString() + " has " + lifetimeXP + " lifetime XP in " + skill.getDisplayName()),
                false
        );
        return 1;
    }

    //Prints the amount of cumulative experience required to reach the target level.
    private static int getXPToReachLvl(CommandContext<ServerCommandSource> ctx) {
        int targetLvl = IntegerArgumentType.getInteger(ctx, "level");

        int totalXP = XPUtil.getTotalXPToReachLevel(targetLvl);
        ctx.getSource().sendFeedback(
                () -> Text.literal("A total of " + totalXP + " is required in a skill to reach level " + targetLvl + " in a skill."),
                false
        );
        return 1;
    }

    //Resets the target players experience in all skills.
    private static int resetPlayerSkills(CommandContext<ServerCommandSource> ctx, String targetName) {
        ServerPlayerEntity player = ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);
        if (player == null) {
            ctx.getSource().sendError(Text.literal("Player not found: " + targetName));
            return 0;
        }

        UUID uuid = player.getUuid();

        for (Skill skill : Skill.values()) {
            PlayerDataManager.setLevel(uuid, PlayerDataManager.STARTING_LEVEL, skill);
            PlayerDataManager.setXP(uuid, PlayerDataManager.STARTING_XP, skill);
            PlayerDataManager.setRemainingXP(uuid, PlayerDataManager.STARTING_REMAINING_XP, skill);
        }

        ctx.getSource().sendFeedback(
                () -> Text.literal("Reset skill data for " + player.getName().getString()), false
        );
        return 1;
    }
}