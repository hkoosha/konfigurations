package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.type.Kind;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static io.koosha.konfiguration.Konfiguration.kFactory;

@SuppressWarnings("WeakerAccess")
public class ZZKonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    final String key = "theKey";

    private final Konfiguration k = kFactory().kombine("def", kFactory().map(
            "meName",
            () -> Collections.singletonMap(key, value)
    ));

    @Test
    public void testCustomValue() {
        K<DummyCustom> custom = k.custom(key, Kind.of(DummyCustom.class));
        Assert.assertSame(custom.v(), value);
    }

}