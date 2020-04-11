package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Konfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.koosha.konfiguration.Konfiguration.kFactory;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public class ExtJacksonJsonSourceTest extends KonfigValueTestMixin {

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

    private Konfiguration k;

    @BeforeMethod
    public void setup() throws Exception {
        this.json = SAMPLE_0;
        this.k = kFactory().jacksonJson("json", () -> this.json);
    }

    protected void update() {
        this.json = SAMPLE_0;
        this.k = ((KonfigurationManager8) this.k.manager())._update();
    }

    public Konfiguration k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k().manager().hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        this.json = SAMPLE_1;
        assertTrue(this.k().manager().hasUpdate());
    }

}
