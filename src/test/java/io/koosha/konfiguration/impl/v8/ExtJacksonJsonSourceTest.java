package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

@SuppressWarnings("RedundantThrows")
public class ExtJacksonJsonSourceTest {

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
        this.k = new ExtJacksonJsonSource(
                "testJacksonSource",
                () -> json,
                ExtJacksonJsonSource::defaultJacksonObjectMapper);
    }

    private void update() {
        this.json = SAMPLE_1;
        this.k = (ExtJacksonJsonSource) this.k.updatedCopy();
    }

    private Source k() {
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
        final List<Integer> before = this.k().list("aIntList", Kind.LIST_INT).v();
        assertEquals(before, asList(1, 0, 2));

        this.update();

        List<Integer> after = this.k().list("aIntList", Kind.LIST_INT).v();
        assertEquals(after, asList(2, 2));
    }

    @Test
    public void testMap() throws Exception {
        // Not wise to change type, but it can happen.

        final Map<String, Integer> before = new HashMap<>(2);
        final Map<String, String> after = new HashMap<>(2);
        before.put("a", 99);
        before.put("c", 22);
        after.put("a", "b");
        after.put("c", "e");

        assertEquals(this.k().map("aMap", Kind.MAP_STRING__INT).v(), before);
        this.update();
        assertEquals(this.k().map("aMap", Kind.MAP_STRING__STRING).v(), after);
    }

    @Test
    public void testSet() throws Exception {
        assertEquals(this.k().set("aSet", Kind.SET_INT).v(), new HashSet<>(asList(1, 2)));
        this.update();
        assertEquals(this.k().set("aSet", Kind.SET_INT).v(), new HashSet<>(asList(1, 2, 3)));
    }


    // BAD CASES

    @Test(expectedExceptions = KfgAssertionException.class,
            dataProvider = "testBadIntDataProvider")
    public void testBadInt(@NotNull final String konfigKey) throws Exception {
        this.k().int_(konfigKey).v();
    }

    @DataProvider
    public static Object[][] testBadIntDataProvider() {
        return new Object[][]{
                {"aString"},
                // {"aInt"},
                {"aBool"},
                {"aIntList"},
                {"aLong"},
                {"aDouble"},
                {"aMap"},
                {"aSet"},
                {"some"},
        };
    }


    @Test(expectedExceptions = KfgAssertionException.class,
            dataProvider = "testBadDoubleDataProvider")
    public void testBadDouble(@NotNull final String konfigKey) throws Exception {
        this.k().double_(konfigKey).v();
    }

    @DataProvider
    public static Object[][] testBadDoubleDataProvider() {
        return new Object[][]{
                {"aString"},
                {"aInt"},
                {"aBool"},
                {"aIntList"},
                {"aLong"},
                // {"aDouble"},
                {"aMap"},
                {"aSet"},
                {"some"},
        };
    }



    @Test(expectedExceptions = KfgAssertionException.class,
            dataProvider = "testBadLongDataProvider")
    public void testBadLong(@NotNull final String konfigKey) throws Exception {
        this.k().long_(konfigKey).v();
    }

    @DataProvider
    public static Object[][] testBadLongDataProvider() {
        return new Object[][]{
                {"aString"},
                // {"aInt"},
                {"aBool"},
                {"aIntList"},
                // {"aLong"},
                {"aDouble"},
                {"aMap"},
                {"aSet"},
                {"some"},
        };
    }


    @Test(expectedExceptions = KfgAssertionException.class,
            dataProvider = "testBadStringDataProvider")
    public void testBadString(@NotNull final String konfigKey) throws Exception {
        this.k().string(konfigKey).v();
    }

    @DataProvider
    public static Object[][] testBadStringDataProvider() {
        return new Object[][]{
                // {"aString"},
                {"aInt"},
                {"aBool"},
                {"aIntList"},
                {"aLong"},
                {"aDouble"},
                {"aMap"},
                {"aSet"},
                {"some"},
        };
    }


}
