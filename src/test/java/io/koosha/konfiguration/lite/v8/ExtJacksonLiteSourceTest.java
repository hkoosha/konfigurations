package io.koosha.konfiguration.lite.v8;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ExtJacksonLiteSourceTest {

    @Test
    public void testWriteBoolean() {
        final ExtJacksonJsonLiteSource testSource =
            new ExtJacksonJsonLiteSource("testSource", "{}");
        testSource.put("my.key", 99L);
        final String serialize = testSource.serialize();
        Assert.assertEquals(serialize, "{\n  \"my\" : {\n    \"key\" : 99\n  }\n}");
    }

}
