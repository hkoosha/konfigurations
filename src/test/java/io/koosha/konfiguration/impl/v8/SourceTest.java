package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Q;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test for {@link Source}
 */
public class SourceTest {

    private static final String INVALID = "invalidAlwaysKey";
    private static final String MISSING = "missingAlwaysKey";
    private static final String NULL = "nullAlwaysKey";

    private static final String BOOL_T = "boolTrueKey";
    private static final String BOOL_F = "boolFalseKey";

    private static final String CHAR_A = "charA";
    private static final String CHAR_B = "charB";

    private static final String STRING_ABC = "stringAbc";

    public static class ExtSampleSource extends Source {

        @Override
        @Nullable
        public KonfigurationManager8 manager() {
            return null;
        }

        @Override
        @NotNull
        public String name() {
            return getClass().getSimpleName();
        }

        @Override
        public boolean has(@NotNull String key, @Nullable Q<?> type) {
            return !MISSING.equals(key);
        }

        @Override
        boolean isNull(@NotNull String key) {
            return !NULL.equals(key);
        }

        @Override
        @NotNull
        Object bool0(@NotNull String key) {
            switch (key) {
                case BOOL_T:
                    return true;
                case BOOL_F:
                    return false;
                case INVALID:
                    return new Object();
                default:
                    throw new RuntimeException("unknown key=" + key);
            }
        }

        @Override
        @NotNull
        Object char0(@NotNull String key) {
            switch (key) {
                case CHAR_A:
                    return 'a';
                case CHAR_B:
                    return "b";
                case INVALID:
                    return "bb";
                default:
                    throw new RuntimeException("unknown key=" + key);
            }
        }

        @Override
        @NotNull
        Object string0(@NotNull String key) {
            switch (key) {
                case STRING_ABC:
                    return "abc";
                case INVALID:
                    return new Object();
                default:
                    throw new RuntimeException("unknown key=" + key);
            }
        }

        @Override
        @NotNull
        Number number0(@NotNull String key) {
            return null;
        }

        @Override
        @NotNull
        Number numberDouble0(@NotNull String key) {
            return null;
        }

        @Override
        @NotNull
        List<?> list0(@NotNull String key, @NotNull Q<? extends List<?>> q) {
            return null;
        }

        @Override
        @NotNull
        Set<?> set0(@NotNull String key, @NotNull Q<? extends Set<?>> type) {
            return null;
        }

        @Override
        @NotNull
        Map<?, ?> map0(@NotNull String key, @NotNull Q<? extends Map<?, ?>> type) {
            return null;
        }

        @Override
        @NotNull
        Object custom0(@NotNull String key, @NotNull Q<?> type) {
            return null;
        }

    }

    private ExtSampleSource source;

    @BeforeMethod
    public void setup() {
        source = new ExtSampleSource();
    }

    // =========================================================================

    @Test(expectedExceptions = KfgAssertionException.class)
    public void testGetManagerAndSetItToNull() {
        this.source.getManagerAndSetItToNull();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testSubset() {
        this.source.subset("");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRegisterSoft() {
        this.source.registerSoft(key -> {
        });
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRegisterSoft1() {
        this.source.registerSoft(key -> {
        }, "");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRegister() {
        this.source.register(key -> {
        });
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRegister1() {
        this.source.register(key -> {
        }, "");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testDeRegister() {
        this.source.deregister(() -> {
            throw new RuntimeException("shouldn't be called");
        });
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testDeRegister1() {
        this.source.deregister(() -> {
            throw new RuntimeException("shouldn't be called");
        }, "");
    }

    // =========================================================================

    @Test
    public void testBoolValueTrue() {
        Assert.assertEquals(this.source.bool(BOOL_T).v(), Boolean.TRUE);
    }

    @Test
    public void testBoolValueFalse() {
        Assert.assertEquals(this.source.bool(BOOL_F).v(), Boolean.FALSE);
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testBoolInvalidValue() {
        this.source.bool(INVALID).v();
    }

    @Test
    public void testBoolNullValue() {
        Assert.assertNull(this.source.bool(NULL).v());
    }

    @Test(expectedExceptions = KfgAssertionException.class)
    public void testBoolMissingValue() {
        this.source.bool(MISSING).v();
    }

    // ---------------------------------

    @Test
    public void testCharValueChar() {
        Assert.assertEquals(this.source.char_(CHAR_A).v(), (Character) 'a');
    }

    @Test
    public void testCharValueString() {
        Assert.assertEquals(this.source.char_(CHAR_B).v(), (Character) 'b');
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testCharInvalidValue() {
        this.source.char_(INVALID).v();
    }

    @Test
    public void testCharNullValue() {
        Assert.assertNull(this.source.char_(NULL).v());
    }

    @Test(expectedExceptions = KfgAssertionException.class)
    public void testCharMissingValue() {
        this.source.char_(MISSING).v();
    }

    // ---------------------------------

    @Test
    public void testStringValue() {
        Assert.assertEquals(this.source.string(STRING_ABC).v(), "abc");
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testStringValueBadString() {
        this.source.string(INVALID).v();
    }

    @Test(expectedExceptions = KfgTypeException.class)
    public void testStringInvalidValue() {
        this.source.string(INVALID).v();
    }

    @Test
    public void testStringNullValue() {
        Assert.assertNull(this.source.string(NULL).v());
    }

    @Test(expectedExceptions = KfgAssertionException.class)
    public void testStringMissingValue() {
        this.source.string(MISSING).v();
    }

}
