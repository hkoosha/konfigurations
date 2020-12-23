package io.koosha.konfiguration.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.TestUtil;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
@SuppressFBWarnings({"CNT_ROUGH_CONSTANT_VALUE", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
public class ExtJacksonYamlSourceTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExtJacksonYamlSourceTest.class);

    static String SAMPLE_0;
    static String SAMPLE_1;

    private String json;

    private ExtJacksonSource k;

    @BeforeClass
    public void init() throws Exception {
        SAMPLE_0 = TestUtil.readResource("sample0.yaml");
        SAMPLE_1 = TestUtil.readResource("sample1.yaml");
    }

    @BeforeMethod
    public void setup() throws Exception {
        this.json = SAMPLE_0;
        this.k = new ExtJacksonSource("testJacksonSource",
            () -> json,
            ExtJacksonSourceYamlHelper::mapper);
    }

    private void update() {
        this.json = SAMPLE_1;
        this.k = (ExtJacksonSource) this.k.updatedCopy();
    }

    private ExtJacksonSource k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k().hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        this.json = SAMPLE_1;
        assertTrue(this.k().hasUpdate());
    }

    // =========================================================================

    @Test
    public void testBool() throws Exception {
        assertEquals(this.k().bool("aBool").v(), Boolean.TRUE);
        this.update();
        assertEquals(this.k().bool("aBool").v(), Boolean.FALSE);
    }

    @Test
    public void testInt() throws Exception {
        assertEquals(this.k().int_("aInt").v(), Integer.valueOf(12));
        this.update();
        assertEquals(this.k().int_("aInt").v(), Integer.valueOf(99));
    }

    @Test
    public void testLong() throws Exception {
        assertEquals(this.k().long_("aLong").v(), (Object) Long.MAX_VALUE);
        this.update();
        assertEquals(this.k().long_("aLong").v(), (Object) Long.MIN_VALUE);
    }

    @Test
    public void testDouble() throws Exception {
        assertEquals(this.k().double_("aDouble").v(), (Double) 3.14);
        this.update();
        assertEquals(this.k().double_("aDouble").v(), (Double) 4.14);
    }

    @Test
    public void testString() throws Exception {
        assertEquals(this.k().string("aString").v(), "hello world");
        this.update();
        assertEquals(this.k().string("aString").v(), "goodbye world");
    }

    @Test
    public void testList() throws Exception {
        final List<Integer> before = this.k().list("aIntList", Kind.INT).v();
        assertEquals(before, asList(1, 0, 2));

        this.update();

        List<Integer> after = this.k().list("aIntList", Kind.INT).v();
        assertEquals(after, asList(2, 2));
    }

    @Test
    public void testSet() throws Exception {
        assertEquals(this.k().set("aSet", Kind.INT).v(), new HashSet<>(asList(1, 2)));
        this.update();
        assertEquals(this.k().set("aSet", Kind.INT).v(), new HashSet<>(asList(1, 2, 3)));
    }

    @Test
    public void testCustom() throws Exception {
        assertEquals(
            this.k().custom("some.nested.userDefined", Kind.of(TestUtil.DummyCustom.class)).v(),
            new TestUtil.DummyCustom("I'm all set", 99)
        );
    }

    // BAD CASES

    @Test(expectedExceptions = KfgMissingKeyException.class,
          dataProvider = "testBadDoubleDataProvider")
    public void testBadDouble(@NotNull final String konfigKey) throws Exception {
        this.k().double_(konfigKey).v();
        LOG.error("testBadDouble: {} did not fail", konfigKey);
    }

    @DataProvider
    public static Object[][] testBadDoubleDataProvider() {
        return new Object[][]{
            {"aString"},
            // {"aInt"},
            {"aBool"},
            {"aIntList"},
            // {"aLong"},
            // {"aDouble"},
            {"aMap"},
            {"aSet"},
            {"some"},
        };
    }

}
