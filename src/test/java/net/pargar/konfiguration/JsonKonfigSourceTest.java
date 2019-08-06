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

// import java.io.File;
// import java.net.URL;
// import java.util.Scanner;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class JsonKonfigSourceTest extends KonfigValueTestMixin {

    public static final String SAMPLE_0 = "{ \"aInt\": 12, \"aBool\": true, " +
            "\"aIntList\": [1, 0, 2], \"aStringList\": [\"a\", \"B\", \"c\"], " +
            "\"aLong\": 9223372036854775807, \"aDouble\": 3.14, \"aMap\": " +
            "{ \"a\": 99, \"c\": 22 }, \"aSet\": [1, 2, 1, 2], \"aString\": " +
            "\"hello world\", \"some\": { \"nested\": { \"key\": 99, " +
            "\"userDefined\" : { \"str\": \"I'm all set\", \"i\": 99 } } } }";

    public static final String SAMPLE_1 = "{ \"aInt\": 99, \"aBool\": false, " +
            "\"aIntList\": [2, 2], \"aStringList\": [\"a\", \"c\"], \"aLong\": " +
            "-9223372036854775808, \"aDouble\": 4.14, \"aMap\": { \"a\": \"b\", " +
            "\"c\": \"e\" }, \"aSet\": [3, 2, 1, 2], \"aString\": \"goodbye world\" }";

    protected String json;
    protected String json0;
    protected String json1;

    private JsonKonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        // URL url0 = getClass().getResource("sample0.json");
        // File file0 = new File(url0.toURI());
        // this.json0 = new Scanner(file0, "UTF8").useDelimiter("\\Z").next();
        this.json0 = JsonKonfigSourceTest.SAMPLE_0;

        // URL url1 = getClass().getResource("sample1.json");
        // File file1 = new File(url1.toURI());
        // this.json1 = new Scanner(file1, "UTF8").useDelimiter("\\Z").next();
        this.json1 = JsonKonfigSourceTest.SAMPLE_1;
    }

    @BeforeMethod
    public void setup() throws Exception {

        json = json0;
        this.k = new JsonKonfigSource(() -> json);
    }

    protected void update() {

        this.json = this.json1;
        this.k = (JsonKonfigSource) this.k.copyAndUpdate();
    }

    public JsonKonfigSource k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(this.k().isUpdatable());
    }

    @Test
    public void testUpdatable() throws Exception {

        json = json1;
        assertTrue(this.k().isUpdatable());
    }

}
