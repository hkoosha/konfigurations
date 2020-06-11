package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.TestUtil;
import io.koosha.konfiguration.type.Kind;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static io.koosha.konfiguration.Konfiguration.kFactory;

@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerCustomValueTest {

    final TestUtil.DummyCustom value = new TestUtil.DummyCustom();

    final String key = "theKey";

    private final Konfiguration k = kFactory().kombine("def", kFactory().map(
        "meName",
        () -> Collections.singletonMap(key, value)
    ));

    @Test
    public void testCustomValue() {
        K<TestUtil.DummyCustom> custom = k.custom(key, Kind.of(TestUtil.DummyCustom.class));
        Assert.assertSame(custom.v(), value);
    }

}
