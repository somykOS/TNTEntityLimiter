package net.somyk.tntqueue.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.somyk.tntqueue.ModConfig.*;
import static net.minecraft.server.command.CommandManager.*;
import static net.somyk.tntqueue.TntQueue.*;

public class ModifyConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal(MOD_ID.toLowerCase())
                .requires(source -> Permissions.check(source, MOD_ID + ".modify") || source.hasPermissionLevel(4))
                .then(literal(maxPrimedTntAmount)
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal(String.format("Current limit is %d", getIntegerValue(maxPrimedTntAmount))), false);
                            return 1;
                        })
                        .then(argument("value", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    try {
                                        return changeMaxPrimedTntAmount(context, IntegerArgumentType.getInteger(context, "value"));
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        )
                )
                .then(literal(maxQueueSize)
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal(String.format("Current queue size is %d", getIntegerValue(maxQueueSize))), false);
                            return 1;
                        })
                        .then(argument("value", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    try {
                                        return changeMaxQueueSize(context, IntegerArgumentType.getInteger(context, "value"));
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        )
                )
        );
    }

    private static int changeMaxPrimedTntAmount(CommandContext<ServerCommandSource> context, int value) {
        setValue(maxPrimedTntAmount, value);
        context.getSource().sendFeedback(() -> Text.literal(String.format("Successfully changed the maximum amount of primed tnt to %d", value)), true);
        return 1;
    }

    private static int changeMaxQueueSize(CommandContext<ServerCommandSource> context, int value) {
        setValue(maxQueueSize, value);
        context.getSource().sendFeedback(() -> Text.literal(String.format("Successfully changed the maximum size of primed tnt queue to %d", value)), true);
        return 1;
    }
}
