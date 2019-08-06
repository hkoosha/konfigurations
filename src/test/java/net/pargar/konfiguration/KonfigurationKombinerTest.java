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


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.*;


public final class KonfigurationKombinerTest {

    private AtomicBoolean flag = new AtomicBoolean(true);

    private final Supplier<Map<String, Object>> sup = () -> flag.get()
                                                            ? singletonMap("xxx", (Object) 12)
                                                            : singletonMap("xxx", (Object) 99);

    private KonfigurationKombiner k;

    @BeforeMethod
    public void setup() {

        this.flag.set(true);
        this.k = new KonfigurationKombiner(new InMemoryKonfigSource(sup));
    }

    @Test
    public void testV1() throws Exception {

        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());
        k.update();

        assertEquals(k.int_("xxx").v(), (Integer) 99);
    }

    @Test(expectedExceptions = KonfigurationTypeException.class)
    public void testV3() throws Exception {

        k.string("xxx");
    }


    @Test
    public void testDoublyUpdate() throws Exception {

        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());
        assertTrue(k.update());
        assertFalse(k.update());

        assertEquals(k.int_("xxx").v(), (Integer) 99);

    }


    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void testNoDefaultValue() {

        k.long_("someblablabla").v();
    }

    @Test
    public void testDefaultValue() {

        assertEquals(k.long_("someblablabla").v(9876L), (Long) 9876L);
    }

}
