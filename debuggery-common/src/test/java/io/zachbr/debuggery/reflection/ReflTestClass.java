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

package io.zachbr.debuggery.reflection;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ReflTestClass {
    private int some;
    private int random;
    private int numbers;
    private ReflSubClass subClass;

    public ReflTestClass(int num, int num1, int num2) {
        some = num;
        random = num1;
        numbers = num2;
        subClass = new ReflSubClass();
    }

    public int[] getSomeNumbers() {
        return new int[]{some, random, numbers};
    }

    public int[] getNumbersPlusParam(int param) {
        return new int[]{some, random, numbers, param};
    }

    public ReflSubClass getSubClass() {
        return subClass;
    }

    public List alwaysReturnsNull() {
        return null;
    }

    public void methodWithLotsOfParams(int i, int j, int k, int l, int m, int n, int o) {
    }

    public static class ReflSubClass {
        public int[] get1234(int num) {
            return new int[]{1, 2, 3, 4, num};
        }
    }
}
