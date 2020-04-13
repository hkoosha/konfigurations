package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.DummyCustom2;
import io.koosha.konfiguration.Typer;
import io.koosha.konfiguration.TestUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static io.koosha.konfiguration.Konfiguration.kFactory;
import static org.testng.Assert.assertEquals;

public class ExtYamlSourceCustomValueTest {

    @Test
    public void testCustomValue() {
        final DummyCustom bang = kFactory().snakeYaml(
                "yaml",
                () -> "bang:\n  str : hello\n  i: 99").custom("bang", new Typer<DummyCustom>() {
        }).vn();
        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
    }

    @Test
    public void testCustomValue2() {
        final DummyCustom2 bang = kFactory().snakeYaml(
                "snake",
                () -> {
                    try {
                        File file = new File(getClass().getResource("/sample2.yaml").getPath());
                        return new Scanner(file, StandardCharsets.UTF_8.name())
                                .useDelimiter("\\Z").next();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                                            .custom("bang", new Typer<DummyCustom2>() {
                                            }).vn();
        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
        assertEquals(bang.olf, TestUtil.mapOf(
                "manga", "panga", "foo", "bar", "baz", "quo"));
        assertEquals(bang.again, "no");
        assertEquals(bang.i, 99);
    }

}
