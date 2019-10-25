package io.koosha.konfiguration;


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