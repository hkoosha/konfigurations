package io.koosha.konfiguration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public class YamlKonfigSourceTest extends KonfigValueTestMixin {

    private String yaml;
    private String yaml0;
    private String yaml1;

    private Konfiguration k;

    @BeforeClass
    public void classSetup() throws Exception {

        // URL url0 = getClass().getResource("sample0.yaml");
        // File file0 = new File(url0.toURI());
        // this.json0 = new Scanner(file0, "UTF8").useDelimiter("\\Z").next();
        this.yaml0 = SAMPLE_0;

        // URL url1 = getClass().getResource("sample1.yaml");
        // File file1 = new File(url1.toURI());
        // this.json1 = new Scanner(file1, "UTF8").useDelimiter("\\Z").next();
        this.yaml1 = SAMPLE_1;
    }

    @BeforeMethod
    public void setup() throws Exception {

        yaml = yaml0;
        this.k = Konfiguration.snakeYaml(() -> yaml);
    }

    @Override
    protected Konfiguration k() {
        return this.k;
    }

    @Override
    protected void update() {

        this.yaml = this.yaml1;
        this.k = this.k.copyAndUpdate();
    }

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(this.k().hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {

        yaml = yaml1;
        assertTrue(this.k().hasUpdate());
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
