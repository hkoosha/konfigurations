package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import io.koosha.konfiguration.TestUtil;
import io.koosha.konfiguration.type.Kind;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertSame;

@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerCustomValueTest {

    final TestUtil.DummyCustom value = new TestUtil.DummyCustom();

    final String key = "theKey";

    private final Konfiguration k = KonfigurationFactory.getInstance().map(
        "meName",
        () -> Collections.singletonMap(key, value)
    );

    @Test
    public void testCustomValue() {
        K<TestUtil.DummyCustom> custom = k.custom(key, Kind.of(TestUtil.DummyCustom.class));
        assertSame(custom.v(), value);
    }

}
