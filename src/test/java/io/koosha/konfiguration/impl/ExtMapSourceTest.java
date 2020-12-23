package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings({"RedundantThrows", "WeakerAccess"})
public class ExtMapSourceTest {

    protected Map<String, Object> map;
    protected Map<String, Object> map0;
    protected Map<String, Object> map1;

    private Konfiguration k;

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
        this.k = KonfigurationFactory.getInstance().map("map", () -> map);
    }

    private void update() {
        this.map = this.map1;
        //noinspection OptionalGetWithoutIsPresent
        this.k.manager().get().updateNow();
    }

    private Konfiguration k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {
        //noinspection OptionalGetWithoutIsPresent
        assertFalse(this.k().manager().get().hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        map = map1;
        //noinspection OptionalGetWithoutIsPresent
        assertTrue(this.k().manager().get().hasUpdate());
    }

    // =========================================================================

    @Test
    public void testBool() throws Exception {
        assertEquals(this.k().bool("aBool").v(), Boolean.TRUE);
        this.update();
        assertEquals(this.k().bool("aBool").v(), Boolean.FALSE);
    }

    @Test
    public void testInt() throws Exception {
        assertEquals(this.k().int_("aInt").v(), Integer.valueOf(12));
        this.update();
        assertEquals(this.k().int_("aInt").v(), Integer.valueOf(99));
    }

    @Test
    public void testLong() throws Exception {
        assertEquals(this.k().long_("aLong").v(), (Object) Long.MAX_VALUE);
        this.update();
        assertEquals(this.k().long_("aLong").v(), (Object) Long.MIN_VALUE);
    }

    @Test
    public void testDouble() throws Exception {
        assertEquals(this.k().double_("aDouble").v(), (Double) 3.14);
        this.update();
        assertEquals(this.k().double_("aDouble").v(), (Double) 4.14);
    }

    @Test
    public void testString() throws Exception {
        assertEquals(this.k().string("aString").v(), "hello world");
        this.update();
        assertEquals(this.k().string("aString").v(), "goodbye world");
    }

    // BAD CASES


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt0() throws Exception {
        this.k().int_("aBool").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt1() throws Exception {
        this.k().int_("aLong").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt2() throws Exception {
        this.k().int_("aString").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt3() throws Exception {
        this.k().int_("aDouble").v();
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadDouble0() throws Exception {
        this.k().double_("aBool").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadDouble1() throws Exception {
        this.k().double_("aLong").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadDouble() throws Exception {
        this.k().double_("aString").v();
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadLong0() throws Exception {
        this.k().long_("aBool").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadLong1() throws Exception {
        this.k().long_("aString").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadLong2() throws Exception {
        this.k().long_("aDouble").v();
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadString0() throws Exception {
        this.k().string("aInt").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadString1() throws Exception {
        this.k().string("aBool").v();
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadString2() throws Exception {
        this.k().string("aIntList").v();
    }

}
