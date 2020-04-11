package io.koosha.konfiguration;

import org.jetbrains.annotations.ApiStatus;

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

}
