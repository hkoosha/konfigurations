package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.function.Supplier;

import static io.koosha.konfiguration.Konfiguration.kFactory;
import static java.util.Collections.singletonMap;

public class MissingKeyTest {


    private boolean returnFourTaee = true;

    private final Supplier<Map<String, ?>> sup = () -> returnFourTaee
            ? singletonMap("xxx", (Object) 12)
            : singletonMap("xxx", (Object) 99);

    private Konfiguration k;

    @BeforeMethod
    public void setup() {
        this.returnFourTaee = true;
        this.k = kFactory().map("map", sup);
    }

    @Test
    public void testMissingKeyNotRaisedUntilVIsNotCalled() {
        k.string("i.do.not.exist");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testMissingKey() {
        k.string("i.do.not.exist").v();
    }

}
