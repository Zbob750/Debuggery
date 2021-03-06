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

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

// todo - this should be made more abstract at some point and merged into -common, it's mostly a copy pasta of bukkit's for now
public abstract class CommandBase implements Command {
    private static final TextComponent PLAYER_USE_ONLY_MSG = TextComponent.of("This command can only be used by players!").color(TextColor.RED);

    private final String name;
    private final String permNode;
    private final boolean requiresPlayer;
    private final boolean shouldShowInHelp;

    public CommandBase(String name, String permNode, boolean requiresPlayer) {
        this(name, permNode, requiresPlayer, true);
    }

    public CommandBase(String name, String permNode, boolean requiresPlayer, boolean shouldShowInHelp) {
        this.name = name;
        this.permNode = permNode;
        this.requiresPlayer = requiresPlayer;
        this.shouldShowInHelp = shouldShowInHelp;
    }

    @Override
    public final void execute(CommandSource source, @NotNull String[] args) {
        if (this.requiresPlayer && !(source instanceof Player)) {
            source.sendMessage(PLAYER_USE_ONLY_MSG);
            return;
        }

        commandLogic(source, args);
    }

    @Override
    public final List<String> suggest(CommandSource source, @NotNull String[] currentArgs) {
        if (this.requiresPlayer && !(source instanceof Player)) {
            source.sendMessage(PLAYER_USE_ONLY_MSG);
            return Collections.emptyList();
        }

        return tabCompleteLogic(source, currentArgs);
    }

    /**
     * Shows help text for this command
     *
     * @param source {@link CommandSource} responsible for sending the message
     * @param args   arguments for the given command
     */
    public final void showHelpText(CommandSource source, @NotNull String[] args) {
        source.sendMessage(TextComponent.of("==== /" + this.getName() + " ===="));
        this.helpLogic(source, args);
    }

    protected abstract void commandLogic(@NotNull CommandSource source, @NotNull String[] args);
    protected abstract void helpLogic(@NotNull CommandSource source, @NotNull String[] args);
    protected abstract List<String> tabCompleteLogic(@NotNull CommandSource source, @NotNull String[] args);

    @Override
    public boolean hasPermission(CommandSource source, @NotNull String[] args) {
        return source.hasPermission(this.permNode);
    }

    /**
     * Gets the name of this command
     * This is also used for registration
     *
     * @return command name
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the permission required to execute this command
     *
     * @return permission
     */
    public final String getPermission() {
        return this.permNode;
    }

    /**
     * Gets if this command requires a player
     *
     * @return if command requires a player
     */
    public final boolean getRequiresPlayer() {
        return this.requiresPlayer;
    }

    /**
     * Gets if this command should show in the plugin help directory
     *
     * @return if command should show in help
     */
    public final boolean shouldShowInHelp() {
        return this.shouldShowInHelp;
    }
}
