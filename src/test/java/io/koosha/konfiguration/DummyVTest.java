package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Test cases for {@link DummyV}.
 */
@SuppressWarnings("RedundantThrows")
public class DummyVTest {

    private final static String key = "sample.key";

    @Test
    public void testRegister() throws Exception {
        final K<?> dummyV = DummyV.string(key);
        assertEquals(dummyV.register(k -> {
        }).id(), -1L);
    }

    @Test
    public void testGetKey() throws Exception {
        final K<?> dummyV = DummyV.string("", key);
        assertSame(dummyV.key(), key);
    }

    @Test
    public void testV() throws Exception {
        long value = 99L;
        K<?> dummyV = DummyV.long_(value, key);
        assertSame(dummyV.v(), value);
    }

    @Test
    public void testVWithDefaultValue() throws Exception {
        long value = 99L;
        K<Long> dummyV = DummyV.long_(key);
        assertSame(dummyV.v(value), value);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testFalse_() throws Exception {
        assertFalse(DummyV.false_().v());
        assertFalse(DummyV.false_().v(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testTrue_() throws Exception {
        assertTrue(DummyV.true_().v());
        assertTrue(DummyV.true_().v(false));
    }

    @Test
    public void testInt_() throws Exception {
        assertEquals(DummyV.int_(99).v(), (Integer) 99);
        assertEquals(DummyV.int_(99).v(88), (Integer) 99);
    }

    @Test
    public void testLong_() throws Exception {
        assertEquals(DummyV.long_(99L).v(), (Long) 99L);
        assertEquals(DummyV.long_(99L).v(88L), (Long) 99L);
    }

    @Test
    public void testDouble_() throws Exception {
        // We are comparing doubles!!
        assertEquals(DummyV.double_(99.9D).v(), (Double) 99.9D);
        assertEquals(DummyV.double_(99.9D).v(99.9D), (Double) 99.9D);
    }

    @Test
    public void testString() throws Exception {
        String value = "xx yy ha ha";
        assertEquals(DummyV.string(value).v(), value);
        assertEquals(DummyV.string(value).v("something"), value);
    }


    @Test
    public void testList() throws Exception {
        List<Object> value = Arrays.asList(new Object(), new Object());
        List<Object> def = Arrays.asList(new Object(), 1, 2, 3);
        assertSame(DummyV.list(value).v(), value);
        assertSame(DummyV.list(value).v(def), value);
    }

    @Test
    public void testMap() throws Exception {
        Map<Object, Object> value = new HashMap<>();
        value.put(new Object(), new Object());
        value.put(new Object(), new Object());

        Map<Object, Object> def = new HashMap<>();
        value.put("a", new Object());
        value.put("b", new Object());

        assertSame(DummyV.map(value).v(), value);
        assertSame(DummyV.map(value).v(def), value);
    }

    @Test
    public void testSet() throws Exception {
        Set<Object> value = new HashSet<>(Arrays.asList(new Object(), new Object()));
        Set<Object> def = new HashSet<>(Arrays.asList(new Object(), 1, 2, 3));
        assertSame(DummyV.set(value).v(), value);
        assertSame(DummyV.set(value).v(def), value);
    }

    @Test
    public void testNull_() throws Exception {
        assertNull(DummyV.null_(Kind.VOID).v());
        assertNull(DummyV.null_(Kind.VOID).v(null));
    }

}
