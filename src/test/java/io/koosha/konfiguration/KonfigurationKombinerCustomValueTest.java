package io.koosha.konfiguration;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    final String key = "theKey";

    private Konfiguration k = Konfiguration.kombine(Konfiguration.inMemory(() -> Collections.singletonMap(
            key,
            value)));

    @Test
    public void testCustomValue() {

        K<DummyCustom> custom = k.custom(key, DummyCustom.class);
        Assert.assertSame(custom.v(), value);
    }

}
