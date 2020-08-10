package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.ext.v8.ExtJacksonJsonSource;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public class ExtJacksonJsonSourceTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExtJacksonJsonSourceTest.class);

    static String SAMPLE_0;
    static String SAMPLE_1;

    private String json;

    private ExtJacksonJsonSource k;

    @BeforeClass
    public void init() throws Exception {
        //noinspection ConstantConditions
        final URI uri0 = ExtJacksonJsonSource.class.getClassLoader()
                                                   .getResource("sample0.json")
                                                   .toURI();
        //noinspection ConstantConditions
        final URI uri1 = ExtJacksonJsonSource.class.getClassLoader()
                                                   .getResource("sample1.json")
                                                   .toURI();
        SAMPLE_0 = new String(Files.readAllBytes(Paths.get(uri0)));
        SAMPLE_1 = new String(Files.readAllBytes(Paths.get(uri1)));
    }

    @BeforeMethod
    public void setup() throws Exception {
        this.json = SAMPLE_0;
        this.k = new ExtJacksonJsonSource("testJacksonSource", () -> json);
    }

    private void update() {
        this.json = SAMPLE_1;
        this.k = (ExtJacksonJsonSource) this.k.updatedCopy();
    }

    private ExtJacksonJsonSource k() {
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
