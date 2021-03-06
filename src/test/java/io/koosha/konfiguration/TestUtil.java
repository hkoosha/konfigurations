package io.koosha.konfiguration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TestUtil {

    private TestUtil() {

    }

    public static String readResource(final String name) {
        final URL resource = TestUtil.class.getClassLoader()
                                           .getResource(name);
        if (resource == null)
            throw new RuntimeException("resource not found: " + name);

        final URI uri0;
        try {
            uri0 = resource.toURI();
        }
        catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }

        final Path path = Paths.get(uri0);

        final byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * The missing factory method in java 8.
     *
     * @param k    first key
     * @param v    first value
     * @param rest rest of key values.
     * @param <K>  key type
     * @param <V>  value type.
     * @return created map.
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    @Contract(pure = true)
    public static <K, V> Map<K, V> mapOf(final K k, final V v, Object... rest) {
        final Map<K, V> map = new HashMap<>();
        map.put(k, v);
        for (int i = 0; i < rest.length; i += 2)
            map.put((K) rest[i], (V) rest[i + 1]);
        return map;
    }

    /**
     * A dummy custom value object, used to test de/serialization frameworks.
     * <p>
     * All fields are final here, only constructor can be utilized.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static final class DummyCustom2 {

        public final String str;
        public final Map<String, String> olf;
        public final int i;
        public final String again;

        public DummyCustom2(final String str, final String again, final Map<String, String> olf, final int i) {
            this.str = str;
            this.olf = olf;
            this.i = i;
            this.again = again;
        }

        @ConstructorProperties({"again", "olf", "i", "str"})
        public DummyCustom2(final String again, final Map<String, String> olf, final int i, final String str) {
            this(str, again, olf, i);
        }

        @Override
        public String toString() {
            return "DummyCustom2[str=" + this.str + ", i=" + this.i + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.str, this.i);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof DummyCustom2))
                return false;
            return Objects.equals(this.i, ((DummyCustom2) obj).i)
                && Objects.equals(this.str, ((DummyCustom2) obj).str);
        }
    }

    /**
     * A dummy custom value object, used to test de/serialization frameworks.
     */
    @SuppressWarnings({"FieldCanBeLocal", "WeakerAccess", "unused"})
    public static final class DummyCustom {

        public String str;
        public int i;

        public DummyCustom() {
            this("", 0);
        }

        @ConstructorProperties({"str", "i"})
        public DummyCustom(final String str, final int i) {
            this.str = str;
            this.i = i;
        }

        public String concat() {
            return this.str + " ::: " + this.i;
        }

        @Override
        public String toString() {
            return "DummyCustom[str=" + this.str + ", i=" + this.i + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.str, this.i);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof DummyCustom))
                return false;
            return Objects.equals(this.i, ((DummyCustom) obj).i)
                && Objects.equals(this.str, ((DummyCustom) obj).str);
        }

    }

}
