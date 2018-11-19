/*
 * This file is part of Debuggery.
 *
 * Debuggery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Debuggery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Debuggery.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.zachbr.debuggery.commands.base;

import io.zachbr.debuggery.DebuggeryBukkit;
import io.zachbr.debuggery.reflection.*;
import io.zachbr.debuggery.reflection.types.InputException;
import io.zachbr.debuggery.util.FancyExceptionWrapper;
import io.zachbr.debuggery.util.PlatformUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Base class for all commands that use reflection to dig into Bukkit's API
 */
public abstract class CommandReflection extends CommandBase {
    private final DebuggeryBukkit debuggery;
    private final MethodMapProvider mapCache;
    private MethodMap availableMethods = MethodMap.EMPTY;

    protected CommandReflection(String name, String permission, boolean requiresPlayer, Class clazz, DebuggeryBukkit plugin) {
        super(name, permission, requiresPlayer);
        this.debuggery = plugin;
        this.mapCache = plugin.getMethodMapProvider();
        updateReflectionClass(clazz);
    }

    @Override
    protected boolean helpLogic(CommandSender sender, String[] args) {
        sender.sendMessage("Uses reflection to call API methods built into Bukkit.");
        sender.sendMessage("Try using the tab completion to see all available subcommands.");
        return true;
    }

    /**
     * Handles all the reflection based command logic
     *
     * @param sender   sender to send information to
     * @param args     command arguments
     * @param instance instance of the class type
     * @return true if handled successfully
     */
    protected boolean doReflectionLookups(CommandSender sender, String[] args, Object instance) {
        // 0 args just return info on object itself

        if (args.length == 0) {
            sender.sendMessage(getOutputStringFor(instance));
            return true;
        }

        // more than 0 args, start chains

        Class activeClass = availableMethods.getMappedClass();
        Validate.isTrue(activeClass.isInstance(instance), "Instance is of type: " + instance.getClass().getSimpleName() + "but was expecting: " + activeClass.getSimpleName());
        final String inputMethod = args[0];

        if (!availableMethods.containsId(inputMethod)) {
            sender.sendMessage(ChatColor.RED + "Unknown or unavailable method");
            return true;
        }

        ReflectionChain.Result chainResult;

        try {
            chainResult = debuggery.performReflectiveChain(args, instance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InputException ex) {
            final String errorMessage = ex instanceof InputException ? "Exception deducing proper types from your input!" : "Exception invoking method - See console for more details!";
            final Throwable cause = ex.getCause() == null ? ex : ex.getCause();

            if (PlatformUtil.canUseFancyChatExceptions()) {
                FancyExceptionWrapper.sendFancyChatException(sender, errorMessage, cause);
            } else {
                sender.sendMessage(ChatColor.RED + errorMessage);
            }

            cause.printStackTrace();
            return true;
        }

        String responseToSender = null;
        switch (chainResult.getType()) {
            case SUCCESS:
                responseToSender = getOutputStringFor(chainResult.getEndingInstance());
                break;
            case NULL_REFERENCE:
            case UNKNOWN_REFERENCE:
                if (chainResult.getReason() != null) {
                    responseToSender = ChatColor.RED + chainResult.getReason();
                }

                break;
            default:
                throw new IllegalStateException("Unhandled switch case for " + chainResult.getType());
        }

        if (responseToSender != null) {
            sender.sendMessage(responseToSender);
        }

        return true;
    }

    /**
     * Updates the locally cached reflection class
     *
     * @param typeIn class type to cache a reflection map for
     */
    protected void updateReflectionClass(Class typeIn) {
        if (availableMethods.getMappedClass() != typeIn) {
            availableMethods = mapCache.getMethodMapFor(typeIn);
        }
    }

    /**
     * Convenience method to run objects past the TypeHandler
     *
     * @param object Object to get String output for
     * @return textual description of Object
     */
    protected @Nullable String getOutputStringFor(@Nullable Object object) {
        return debuggery.getTypeHandler().getOutputFor(object);
    }

    @Override
    public List<String> tabCompleteLogic(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = Arrays.asList(args);
        MethodMap reflectionMap = this.availableMethods;
        Method lastMethod = null;
        Class returnType = this.availableMethods.getMappedClass();

        int argsToSkip = 0;

        for (int i = 0; i < arguments.size(); i++) {
            String currentArg = arguments.get(i);
            if (argsToSkip > 0) {
                argsToSkip--;
                reflectionMap = null;

                continue;
            }

            reflectionMap = mapCache.getMethodMapFor(returnType);

            if (reflectionMap.getById(currentArg) != null) {
                lastMethod = reflectionMap.getById(currentArg);
                List<String> stringMethodArgs = ReflectionUtil.getArgsForMethod(arguments.subList(i + 1, arguments.size()), lastMethod);
                argsToSkip = stringMethodArgs.size();

                returnType = lastMethod.getReturnType();
            }
        }

        return reflectionMap == null ? Collections.emptyList() : getCompletionsMatching(args, reflectionMap.getAllIds());
    }
}