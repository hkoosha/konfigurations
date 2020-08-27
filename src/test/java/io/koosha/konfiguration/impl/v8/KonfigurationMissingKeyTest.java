package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class KonfigurationMissingKeyTest {

    private boolean returnFour = true;

    private final Supplier<Map<String, ?>> sup = () -> returnFour
        ? singletonMap("xxx", (Object) 12)
        : singletonMap("xxx", (Object) 99);

    private Konfiguration k;

    @BeforeMethod
    public void setup() {
        this.returnFour = true;
        this.k = KonfigurationFactory.getInstance()
                                     .map("map", sup);
    }

    @Test
    public void testMissingKeyNotRaisedUntilValueIsNotCalled() {
        k.string("i.do.not.exist");
    }

    @Test
    public void testMissingKeyNotRaisedWhenDefaultIsProvidedWithNull() {
        final String v = k.string("i.do.not.exist").v(null);
        assertNull(v);
    }

    @Test
    public void testMissingKeyNotRaisedWhenDefaultIsProvidedWithNonNull() {
        final String v = k.string("i.do.not.exist").v("haha");
        assertEquals(v, "haha");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testMissingKeyThrowsException() {
        k.string("i.do.not.exist").v();
    }

}
