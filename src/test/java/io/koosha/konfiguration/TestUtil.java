package io.koosha.konfiguration;

import org.jetbrains.annotations.ApiStatus;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

public final class TestUtil {

    private TestUtil() {

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
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.Internal
    public static <K, V> Map<K, V> mapOf(final K k, final V v, Object... rest) {
        final Map map = new HashMap<>();
        map.put(k, v);
        for (int i = 0; i < rest.length; i += 2) {
            map.put(rest[i], rest[i + 1]);
        }
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

        @ConstructorProperties({"str", "again", "olf", "i"})
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

    }

}
