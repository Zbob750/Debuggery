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

package com.destroystokyo.debuggery.reflection.types.handlers.input;

import com.destroystokyo.debuggery.reflection.types.handlers.base.IHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class IItemStacksHandler implements IHandler {
    @Nonnull
    @Override
    public Object instantiateInstance(String input, Class clazz, @Nullable CommandSender sender) {
        List<ItemStack> stacksOut = new ArrayList<>();
        String[] stacksIn = input.split(",");

        for (String stack : stacksIn) {
            stacksOut.add(IItemStackHandler.getItemStack(stack, sender));
        }

        return stacksOut.toArray(new ItemStack[0]);
    }

    @Nonnull
    @Override
    public Class<?> getRelevantClass() {
        return ItemStack[].class;
    }
}