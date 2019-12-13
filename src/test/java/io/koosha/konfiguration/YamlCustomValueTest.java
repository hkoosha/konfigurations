package io.koosha.konfiguration;

import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;

public class YamlCustomValueTest {

    @Test
    public void testCustomValue() {
        final DummyCustom bang = Konfiguration.snakeYaml(
                () -> "bang:\n  str : hello\n  i: 99")
                .custom("bang", DummyCustom.class);
        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
    }

    @Test
    public void testCustomValue2() {
        final DummyCustom2 bang = Konfiguration.snakeYaml(
                () -> {
                    try {
                        return new Scanner(new File(
                                getClass().getResource("/sample2.yaml").toURI()),
                                           StandardCharsets.UTF_8)
                                .useDelimiter("\\Z").next();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .custom("bang", DummyCustom2.class);
        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
        assertEquals(bang.olf, Map.of(
                "manga", "panga", "foo", "bar", "baz", "quo"));
        assertEquals(bang.again, "no");
        assertEquals(bang.i, 99);
    }

}
