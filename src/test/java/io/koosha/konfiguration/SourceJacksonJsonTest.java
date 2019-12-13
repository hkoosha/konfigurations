package io.koosha.konfiguration;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// import java.io.File;
// import java.net.URL;
// import java.util.Scanner;


@SuppressWarnings("RedundantThrows")
public class SourceJacksonJsonTest extends KonfigValueTestMixin {

    static final String SAMPLE_0 = "{ \"aInt\": 12, \"aBool\": true, " +
            "\"aIntList\": [1, 0, 2], \"aStringList\": [\"a\", \"B\", \"c\"], " +
            "\"aLong\": 9223372036854775807, \"aDouble\": 3.14, \"aMap\": " +
            "{ \"a\": 99, \"c\": 22 }, \"aSet\": [1, 2, 1, 2], \"aString\": " +
            "\"hello world\", \"some\": { \"nested\": { \"key\": 99, " +
            "\"userDefined\" : { \"str\": \"I'm all set\", \"i\": 99 } } } }";

    static final String SAMPLE_1 = "{ \"aInt\": 99, \"aBool\": false, " +
            "\"aIntList\": [2, 2], \"aStringList\": [\"a\", \"c\"], \"aLong\": " +
            "-9223372036854775808, \"aDouble\": 4.14, \"aMap\": { \"a\": \"b\", " +
            "\"c\": \"e\" }, \"aSet\": [3, 2, 1, 2], \"aString\": \"goodbye world\" }";

    private String json;
    private String json0;
    private String json1;

    private Konfiguration k;

    @BeforeClass
    public void classSetup() throws Exception {

        // URL url0 = getClass().getResource("sample0.json");
        // File file0 = new File(url0.toURI());
        // this.json0 = new Scanner(file0, "UTF8").useDelimiter("\\Z").next();
        this.json0 = SourceJacksonJsonTest.SAMPLE_0;

        // URL url1 = getClass().getResource("sample1.json");
        // File file1 = new File(url1.toURI());
        // this.json1 = new Scanner(file1, "UTF8").useDelimiter("\\Z").next();
        this.json1 = SourceJacksonJsonTest.SAMPLE_1;
    }

    @BeforeMethod
    public void setup() throws Exception {

        json = json0;
        this.k = Konfiguration.jacksonJson(() -> json);
    }

    protected void update() {

        this.json = this.json1;
        this.k = this.k.copyAndUpdate();
    }

    public Konfiguration k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(this.k().hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {

        json = json1;
        assertTrue(this.k().hasUpdate());
    }

}
