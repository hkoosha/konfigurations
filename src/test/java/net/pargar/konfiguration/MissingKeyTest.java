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
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;


public class MissingKeyTest {


    private boolean returnFourTaee = true;

    private final Supplier<Map<String, Object>> sup = () -> returnFourTaee
                                                            ? singletonMap("xxx", (Object) 12)
                                                            : singletonMap("xxx", (Object) 99);

    private KonfigurationKombiner k;

    @BeforeMethod
    public void setup() {

        this.returnFourTaee = true;
        this.k = new KonfigurationKombiner(new InMemoryKonfigSource(sup));
    }

    @Test
    public void testMissingKeyNotRaisedUntilVIsNotCalled() {

        k.string("i.do.not.exist");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void testMissingKey() {

        k.string("i.do.not.exist").v();
    }

}
