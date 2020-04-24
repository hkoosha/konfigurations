package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.koosha.konfiguration.Konfiguration.kFactory;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public class ExtYamlSourceTest extends KonfigValueTestMixin {

    static String SAMPLE_0;
    static String SAMPLE_1;

    private String yaml;

    private Konfiguration k;
    private KonfigurationManager man;

    @BeforeClass
    public void init() throws Exception {
        //noinspection ConstantConditions
        final URI uri0 = ExtYamlSource.class.getClassLoader()
                                            .getResource("sample0.yaml")
                                            .toURI();
        //noinspection ConstantConditions
        final URI uri1 = ExtYamlSource.class.getClassLoader()
                                            .getResource("sample1.yaml")
                                            .toURI();
        SAMPLE_0 = new String(Files.readAllBytes(Paths.get(uri0)));
        SAMPLE_1 = new String(Files.readAllBytes(Paths.get(uri1)));
    }

    @BeforeMethod
    public void setup() throws Exception {
        this.yaml = SAMPLE_0;
        this.k = kFactory().snakeYaml("meNameYaml", () -> yaml);
        this.man = this.k.manager();
    }

    @Override
    protected Konfiguration k() {
        return this.k;
    }

    @Override
    protected void update() {
        this.yaml = SAMPLE_0;
        this.man.update();
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.man.hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        this.yaml = SAMPLE_1;
        assertTrue(this.man.hasUpdate());
    }

}
