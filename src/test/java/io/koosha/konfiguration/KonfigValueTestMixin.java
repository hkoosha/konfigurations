package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

/**
 * Base test cases. Must be extended.
 */
@SuppressWarnings("RedundantThrows")
public abstract class KonfigValueTestMixin {

    protected abstract Konfiguration k();

    protected abstract void update();


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


    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadInt0() throws Exception {
        this.k().int_("aBool").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadInt1() throws Exception {
        this.k().int_("aLong").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadInt2() throws Exception {
        this.k().int_("aString").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadInt3() throws Exception {
        this.k().int_("aDouble").v();
    }


    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadDouble0() throws Exception {
        this.k().double_("aBool").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadDouble1() throws Exception {
        this.k().double_("aLong").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadDouble() throws Exception {
        this.k().double_("aString").v();
    }


    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadLong0() throws Exception {
        this.k().long_("aBool").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadLong1() throws Exception {
        this.k().long_("aString").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadLong2() throws Exception {
        this.k().long_("aDouble").v();
    }


    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadString0() throws Exception {
        this.k().string("aInt").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadString1() throws Exception {
        this.k().string("aBool").v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadString2() throws Exception {
        this.k().string("aIntList").v();
    }


}
