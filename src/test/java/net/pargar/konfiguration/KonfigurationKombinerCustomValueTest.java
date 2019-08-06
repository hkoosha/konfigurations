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


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    final String key = "theKey";

    private KonfigurationKombiner k = new KonfigurationKombiner(new InMemoryKonfigSource(() -> Collections.singletonMap(
            key,
            value)));

    @Test
    public void testCustomValue() {

        K<DummyCustom> custom = k.custom(key, DummyCustom.class);
        Assert.assertSame(custom.v(), value);
    }

}
