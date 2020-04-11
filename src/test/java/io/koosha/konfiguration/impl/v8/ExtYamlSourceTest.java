package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Konfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.koosha.konfiguration.Konfiguration.kFactory;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public class ExtYamlSourceTest extends KonfigValueTestMixin {

    private String yaml;

    private Konfiguration k;

    @BeforeMethod
    public void setup() throws Exception {
        this.k = kFactory().snakeYaml("meNameYaml", () -> yaml);
    }

    @Override
    protected Konfiguration k() {
        return this.k;
    }

    @Override
    protected void update() {
        this.yaml = SAMPLE_0;
        this.k = ((KonfigurationManager8) this.k.manager())._update();
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k().manager().hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        this.yaml = SAMPLE_1;
        assertTrue(this.k().manager().hasUpdate());
    }


    private static final String SAMPLE_0 = "aInt: 12\n" +
            "aBool: true\n" +
            "aIntList: \n" +
            "  - 1\n" +
            "  - 0\n" +
            "  - 2\n" +
            "aStringList: [\"a\", \"B\", \"c\"]\n" +
            "aLong: 9223372036854775807\n" +
            "aDouble: 3.14\n" +
            "aMap:\n" +
            "    \"a\": 99\n" +
            "    \"c\": 22\n" +
            "aSet: [1, 2]\n" +
            "aString: \"hello world\"\n" +
            "\n" +
            "some:\n" +
            "    nested: \n" +
            "        key: 99\n" +
            "        userDefined: \n" +
            "            str: \"I'm all set\"\n" +
            "            i: 99\n" +
            "        \n";

    private static final String SAMPLE_1 = "aInt: 99\n" +
            "aBool: false\n" +
            "aIntList: [2, 2]\n" +
            "aStringList: [\"a\", \"c\"]\n" +
            "aLong: -9223372036854775808\n" +
            "aDouble: 4.14\n" +
            "aMap: \n" +
            "   a: \"b\"\n" +
            "   c: \"e\"\n" +
            "aSet: [3, 2, 1]\n" +
            "aString: \"goodbye world\"\n";

}
