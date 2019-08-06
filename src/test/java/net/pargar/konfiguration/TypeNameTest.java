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

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class TypeNameTest {

    @Test
    public void testGetTName() throws Exception {
        assertEquals(TypeName.STRING_ARRAY.getTName(), "string array");
    }

    @Test
    public void testTypeName() throws Exception {
        assertEquals(TypeName.typeName(null, null), "?");

        assertEquals(TypeName.typeName(String.class, null), String.class.getName());

        assertEquals(TypeName.typeName(null, Long.class), Long.class.getName());

        assertEquals(TypeName.typeName(ArrayList.class, List.class),
                     ArrayList.class.getName() + "/" + List.class.getName());
    }

}
