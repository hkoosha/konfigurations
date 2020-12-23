package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import io.koosha.konfiguration.KonfigurationManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.koosha.konfiguration.TestUtil.mapOf;
import static java.util.Arrays.asList;

@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerConcurrencyTest {

    private Map<String, Object> MAP0;
    private Map<String, Object> MAP1;
    private Map<String, Object> MAP2;
    private String JSON0;
    private String JSON1;

    volatile boolean run = true;
    long c = 0;

    private Map<String, Object> map;
    private String json;
    private Konfiguration k;
    private KonfigurationManager man;

    @BeforeClass
    public void init() throws Exception {
        //noinspection ConstantConditions
        final URI uri0 = ExtJacksonSource.class.getClassLoader()
                                               .getResource("sample0.json")
                                               .toURI();
        //noinspection ConstantConditions
        final URI uri1 = ExtJacksonSource.class.getClassLoader()
                                               .getResource("sample1.json")
                                               .toURI();
        JSON0 = new String(Files.readAllBytes(Paths.get(uri0)));
        JSON1 = new String(Files.readAllBytes(Paths.get(uri1)));

        this.MAP0 = mapOf("aInt", 12, "aBool", false, "aIntList", asList(1, 0, 2), "aLong", 88L);
        this.MAP1 = mapOf("aInt", 99, "bBool", false, "aIntList", asList(2, 2));
        this.MAP2 = mapOf("xx", 44, "yy", true);
    }

    @BeforeMethod
    void reset() {
        this.map = this.MAP0;
        this.json = this.JSON0;

        this.k = KonfigurationFactory.getInstance().kombine(
            "kombine",
            KonfigurationFactory.getInstance().map("map", () -> this.map),
            KonfigurationFactory.getInstance().map("map", () -> this.MAP2),
            KonfigurationFactory.getInstance().jacksonJson("json", () -> this.json)
        );
        //noinspection OptionalGetWithoutIsPresent
        this.man = this.k.manager().get();
    }

    private synchronized void toggle() {
        this.json = Objects.equals(this.json, JSON0) ? JSON1 : JSON0;
        this.map = Objects.equals(this.map, MAP0) ? MAP1 : MAP0;
    }

    @Test(enabled = false)
    public void benchmark() {
        ExecutorService e = null;
        try {
            e = Executors.newSingleThreadExecutor();
            e.submit(() -> {
                while (run) {
                    KonfigurationKombinerConcurrencyTest.this.toggle();
                    this.man.updateNow();
                    c++;
                }
            });

            final long loops = 1_000_000L;

            long total = 0;
            for (int i = 0; i < loops; i++) {
                final long b = System.currentTimeMillis();
                k.int_("xxx" + i).v(2);
                k.int_("aInt").v();
                total += System.currentTimeMillis() - b;
            }

            run = false;

            System.out.println("total time: " + total);
            System.out.println("each cycle time:" + ((double) total) / loops);
            System.out.println("total update count: " + c);
        }
        finally {
            if (e != null)
                e.shutdown();
        }
    }

    // This test is plain wrong.
    @Test(enabled = false)
    public void testMissedUpdates() {
        ExecutorService e = null;
        try {
            e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                e.submit(() -> {
                    while (run) {
                        KonfigurationKombinerConcurrencyTest.this.toggle();
                        this.man.updateNow();
                    }
                });
            }

            for (int i = 0; i < 10_000; i++) {
                final Integer value = k.int_("aInt").v();
                Assert.assertEquals(value, (Integer) 12);
                if (i % 1000 == 0)
                    System.out.println(i);
            }

            run = false;
        }
        finally {
            if (e != null)
                e.shutdown();
        }
    }

}
