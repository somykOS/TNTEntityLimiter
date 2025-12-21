package net.somyk.tntqueue.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.network.chat.Component;

import static net.somyk.tntqueue.ModConfig.*;
import static net.somyk.tntqueue.TntQueue.*;

public class ModifyConfigCommand {

  public static void register(
      CommandDispatcher<CommandSourceStack> dispatcher,
      CommandBuildContext commandRegistryAccess,
      CommandSelection commandSelection) {
    dispatcher.register(
        Commands.literal(MOD_ID.toLowerCase())
            .requires(
                (source) ->
                    Permissions.check(source, MOD_ID + ".modify", false) || source.hasPermission(4))
            .then(
                Commands.literal(maxPrimedTntAmount)
                    .executes(
                        context -> {
                          context
                              .getSource()
                              .sendSuccess(
                                  () ->
                                      Component.literal(
                                          String.format(
                                              "Current limit is %d",
                                              getIntegerValue(maxPrimedTntAmount))),
                                  false);
                          return 1;
                        })
                    .then(
                        Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(
                                context -> {
                                  try {
                                    return changeMaxPrimedTntAmount(
                                        context, IntegerArgumentType.getInteger(context, "value"));
                                  } catch (Exception e) {
                                    throw new RuntimeException(e);
                                  }
                                })))
            .then(
                Commands.literal(maxQueueSize)
                    .executes(
                        context -> {
                          context
                              .getSource()
                              .sendSuccess(
                                  () ->
                                      Component.literal(
                                          String.format(
                                              "Current queue size is %d",
                                              getIntegerValue(maxQueueSize))),
                                  false);
                          return 1;
                        })
                    .then(
                        Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(
                                context -> {
                                  try {
                                    return changeMaxQueueSize(
                                        context, IntegerArgumentType.getInteger(context, "value"));
                                  } catch (Exception e) {
                                    throw new RuntimeException(e);
                                  }
                                }))));
  }

  private static int changeMaxPrimedTntAmount(
      CommandContext<CommandSourceStack> context, int value) {
    setValue(maxPrimedTntAmount, value);
    context
        .getSource()
        .sendSuccess(
            () ->
                Component.literal(
                    String.format(
                        "Successfully changed the maximum amount of primed tnt to %d", value)),
            true);
    return 1;
  }

  private static int changeMaxQueueSize(CommandContext<CommandSourceStack> context, int value) {
    setValue(maxQueueSize, value);
    context
        .getSource()
        .sendSuccess(
            () ->
                Component.literal(
                    String.format(
                        "Successfully changed the maximum size of primed tnt queue to %d", value)),
            true);
    return 1;
  }
}
