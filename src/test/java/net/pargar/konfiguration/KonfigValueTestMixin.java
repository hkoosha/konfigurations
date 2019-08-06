/*
 * Copyright (C) 2019 Koosha Hosseiny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.pargar.konfiguration;


import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;


public abstract class KonfigValueTestMixin {

    private static final String ERR_MSG_TEMPLATE = "required type=.*, but found=.* for key=.*";

    protected abstract KonfigSource k();

    protected abstract void update();


    @Test
    public void testBool() throws Exception {

        assertEquals(this.k().bool("aBool"), Boolean.TRUE);
        this.update();
        assertEquals(this.k().bool("aBool"), Boolean.FALSE);
    }

    @Test
    public void testInt() throws Exception {

        assertEquals(this.k().int_("aInt"), Integer.valueOf(12));
        this.update();
        assertEquals(this.k().int_("aInt"), Integer.valueOf(99));
    }

    @Test
    public void testLong() throws Exception {

        assertEquals(this.k().long_("aLong"), (Object) Long.MAX_VALUE);
        this.update();
        assertEquals(this.k().long_("aLong"), (Object) Long.MIN_VALUE);
    }

    @Test
    public void testDouble() throws Exception {

        assertEquals(this.k().double_("aDouble"), 3.14);
        this.update();
        assertEquals(this.k().double_("aDouble"), 4.14);
    }

    @Test
    public void testString() throws Exception {

        assertEquals(this.k().string("aString"), "hello world");
        this.update();
        assertEquals(this.k().string("aString"), "goodbye world");
    }

    @Test
    public void testList() throws Exception {

        final List<Integer> before = this.k().list("aIntList", int.class);
        assertEquals(before, asList(1, 0, 2));

        this.update();

        List<Integer> after = this.k().list("aIntList", int.class);
        assertEquals(after, asList(2, 2));
    }

    @Test
    public void testMap() throws Exception {

        // Not wise to change type, but it can happen.

        final Map<String, Integer> before = new HashMap<>(2);
        final Map<String, String> after = new HashMap<>(2);
        before.put("a", 99);
        before.put("c", 22);
        after.put("a", "b");
        after.put("c", "e");

        assertEquals(this.k().map("aMap", int.class), before);
        this.update();
        assertEquals(this.k().map("aMap", String.class), after);
    }

    @Test
    public void testSet() throws Exception {

        assertEquals(this.k().set("aSet", int.class), new HashSet<>(asList(1, 2)));
        this.update();
        assertEquals(this.k().set("aSet", int.class), new HashSet<>(asList(1, 2, 3)));
    }


    // BAD CASES


    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadInt0() throws Exception {
        this.k().int_("aBool");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadInt1() throws Exception {

        this.k().int_("aLong");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadInt2() throws Exception {

        this.k().int_("aString");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadInt3() throws Exception {

        this.k().int_("aDouble");
    }


    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadDouble0() throws Exception {

        this.k().double_("aBool");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadDouble1() throws Exception {

        this.k().double_("aLong");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadDouble() throws Exception {

        this.k().double_("aString");
    }


    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadLong0() throws Exception {

        this.k().long_("aBool");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadLong1() throws Exception {

        this.k().long_("aString");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadLong2() throws Exception {

        this.k().long_("aDouble");
    }


    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadString0() throws Exception {

        this.k().string("aInt");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadString1() throws Exception {

        this.k().string("aBool");
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadString2() throws Exception {

        this.k().string("aIntList");
    }


    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadList0() throws Exception {

        this.k().list("aInt", Integer.class);
    }

    @Test(expectedExceptions = KonfigurationTypeException.class,
          expectedExceptionsMessageRegExp = ERR_MSG_TEMPLATE)
    public void testBadList1() throws Exception {

        this.k().list("aString", String.class);
    }

}
