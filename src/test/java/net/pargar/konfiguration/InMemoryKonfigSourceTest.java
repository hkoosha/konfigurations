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


import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


@SuppressWarnings("RedundantThrows")
public class InMemoryKonfigSourceTest extends KonfigValueTestMixin {

    protected Map<String, Object> map;
    protected Map<String, Object> map0;
    protected Map<String, Object> map1;

    private InMemoryKonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {
        this.map0 = new HashMap<>();

        this.map0.put("aInt", 12);
        this.map0.put("aBool", true);
        this.map0.put("aIntList", asList(1, 0, 2));
        this.map0.put("aStringList", asList("a", "B", "c"));
        this.map0.put("aLong", Long.MAX_VALUE);
        this.map0.put("aDouble", 3.14D);
        this.map0.put("aString", "hello world");

        HashMap<Object, Object> m0 = new HashMap<>();
        m0.put("a", 99);
        m0.put("c", 22);
        this.map0.put("aMap", m0);

        HashSet<Integer> s0 = new HashSet<>(asList(1, 2));
        this.map0.put("aSet", s0);
        this.map0 = Collections.unmodifiableMap(this.map0);

        // --------------

        this.map1 = new HashMap<>();

        this.map1.put("aInt", 99);
        this.map1.put("aBool", false);
        this.map1.put("aIntList", asList(2, 2));
        this.map1.put("aStringList", asList("a", "c"));
        this.map1.put("aLong", Long.MIN_VALUE);
        this.map1.put("aDouble", 4.14D);
        this.map1.put("aString", "goodbye world");

        HashMap<Object, Object> m1 = new HashMap<>();
        m1.put("a", "b");
        m1.put("c", "e");
        this.map1.put("aMap", m1);

        HashSet<Integer> s1 = new HashSet<>(asList(1, 2, 3));
        this.map1.put("aSet", s1);

        this.map1 = Collections.unmodifiableMap(this.map1);
    }

    @BeforeMethod
    public void setup() throws Exception {

        this.map = this.map0;
        this.k = new InMemoryKonfigSource(() -> map);
    }

    @Override
    protected void update() {
        this.map = this.map1;
        this.k = (InMemoryKonfigSource) this.k.copyAndUpdate();
    }

    public InMemoryKonfigSource k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k().isUpdatable());
    }

    @Test
    public void testUpdatable() throws Exception {
        map = map1;
        assertTrue(this.k().isUpdatable());
    }

}
