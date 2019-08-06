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

import java.util.*;

import static org.testng.Assert.*;


@SuppressWarnings("RedundantThrows")
public class DummyVTest {

    private final static String key = "sample.key";

    @Test
    public void testDeregister() throws Exception {
        DummyV<?> dummyV = new DummyV<>(key, null);
        assertSame(dummyV.deregister((KeyObserver) k -> {
        }), dummyV);
    }

    @Test
    public void testRegister() throws Exception {
        DummyV<?> dummyV = new DummyV<>(key);
        assertSame(dummyV.register((KeyObserver) k -> {
        }), dummyV);
    }

    @Test
    public void testGetKey() throws Exception {
        DummyV<?> dummyV = new DummyV<>(key);
        assertSame(dummyV.getKey(), key);
    }

    @Test
    public void testV() throws Exception {
        long value = 99L;
        DummyV<?> dummyV = new DummyV<>(key, value);
        assertSame(dummyV.v(), value);
    }

    @Test
    public void testVWithDefaultValue() throws Exception {
        long value = 99L;
        DummyV<Long> dummyV = new DummyV<>(key);
        assertSame(dummyV.v(value), value);
    }

    @Test
    public void testFalse_() throws Exception {
        assertFalse(DummyV.false_().v());
        assertFalse(DummyV.false_().v(true));
    }

    @Test
    public void testTrue_() throws Exception {
        assertTrue(DummyV.true_().v());
        assertTrue(DummyV.true_().v(false));
    }

    @Test
    public void testInt_() throws Exception {
        assertEquals(DummyV.int_(99).v(), (Integer) 99);
        assertEquals(DummyV.int_(99).v(88), (Integer) 99);
    }

    @Test
    public void testLong_() throws Exception {
        assertEquals(DummyV.long_(99L).v(), (Long) 99L);
        assertEquals(DummyV.long_(99L).v(88L), (Long) 99L);
    }

    @Test
    public void testDouble_() throws Exception {
        // We are comparing doubles!!
        assertEquals(DummyV.double_(99.9D).v(), 99.9D);
        assertEquals(DummyV.double_(99.9D).v(99.9D), 99.9D);
    }

    @Test
    public void testString() throws Exception {
        String value = "haha";
        assertEquals(DummyV.string(value).v(), value);
        assertEquals(DummyV.string(value).v("something"), value);
    }

    @Test
    public void testEmptyCollection() throws Exception {
        Collection<Object> value = Arrays.asList(new Object(), new Object());
        assertTrue(DummyV.emptyCollection().v().isEmpty());
        assertTrue(DummyV.emptyCollection().v(value).isEmpty());
    }

    @Test
    public void testEmptyList() throws Exception {
        List<Object> value = Arrays.asList(new Object(), new Object());
        assertTrue(DummyV.emptyList().v().isEmpty());
        assertTrue(DummyV.emptyList().v(value).isEmpty());
    }

    @Test
    public void testEmptyMap() throws Exception {
        Map<Object, Object> value = new HashMap<>();
        value.put(new Object(), new Object());
        value.put(new Object(), new Object());
        assertTrue(DummyV.emptyMap().v().isEmpty());
        assertTrue(DummyV.emptyMap().v(value).isEmpty());
    }

    @Test
    public void testEmptySet() throws Exception {
        Set<Object> value = new HashSet<>(Arrays.asList(new Object(), new Object()));
        assertTrue(DummyV.emptySet().v().isEmpty());
        assertTrue(DummyV.emptySet().v(value).isEmpty());
    }

    @Test
    public void testList() throws Exception {
        List<Object> value = Arrays.asList(new Object(), new Object());
        List<Object> def = Arrays.asList(new Object(), 1, 2, 3);
        assertSame(DummyV.list(value).v(), value);
        assertSame(DummyV.list(value).v(def), value);
    }

    @Test
    public void testMap() throws Exception {
        Map<Object, Object> value = new HashMap<>();
        value.put(new Object(), new Object());
        value.put(new Object(), new Object());

        Map<Object, Object> def = new HashMap<>();
        value.put("a", new Object());
        value.put("b", new Object());

        assertSame(DummyV.map(value).v(), value);
        assertSame(DummyV.map(value).v(def), value);
    }

    @Test
    public void testSet() throws Exception {
        Set<Object> value = new HashSet<>(Arrays.asList(new Object(), new Object()));
        Set<Object> def = new HashSet<>(Arrays.asList(new Object(), 1, 2, 3));
        assertSame(DummyV.set(value).v(), value);
        assertSame(DummyV.set(value).v(def), value);
    }

    @Test
    public void testNull_() throws Exception {
        assertNull(DummyV.null_().v());
        assertNull(DummyV.null_().v(new Object()));
    }

}
