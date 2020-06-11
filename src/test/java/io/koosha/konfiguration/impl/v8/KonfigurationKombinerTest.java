package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static io.koosha.konfiguration.Konfiguration.kFactory;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public final class KonfigurationKombinerTest {

    private final AtomicBoolean flag = new AtomicBoolean(true);

    private final Supplier<Map<String, ?>> sup = () -> flag.get()
        ? singletonMap("xxx", 12)
        : singletonMap("xxx", 99);

    private Konfiguration k;
    private KonfigurationManager man;

    @BeforeMethod
    public void setup() {
        this.flag.set(true);
        this.k = kFactory().kombine("def", kFactory().map("map", sup));
        this.man = k.manager().get();
    }

    @Test
    public void testV1() throws Exception {
        assertEquals((Object) k.int_("xxx").v(), 12);

        flag.set(!flag.get());
        this.man.updateNow();

        assertEquals((Object) k.int_("xxx").v(), 99);
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testV3() throws Exception {
        k.string("xxx").v();
    }


    @Test
    public void testDoublyUpdate() throws Exception {
        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());
        assertTrue(this.man.updateNow());
        assertFalse(this.man.updateNow());

        assertEquals(k.int_("xxx").v(), (Integer) 99);
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testNoDefaultValue() {
        k.long_("some bla bla bla").v();
    }

    @Test
    public void testDefaultValue() {
        assertEquals(k.long_("someblablabla").v(9876L), (Long) 9876L);
    }

}
