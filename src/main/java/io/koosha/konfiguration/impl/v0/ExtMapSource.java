package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@ThreadSafe
@ApiStatus.Internal
final class ExtMapSource extends Source {

    private static final Pattern DOT = Pattern.compile(Pattern.quote("."));

    private final Supplier<Map<String, ?>> map;
    private final Map<String, ?> root;
    private final int lastHash;
    private final boolean enableNestedMap;

    @NotNull
    private final String name;

    @NotNull
    private final KonfigurationManager0 manager = new KonfigurationManager0() {

        /**
         * {@inheritDoc}
         */
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            final Map<String, ?> newMap = map.get();
            if (newMap == null)
                return false;
            final int newHash = newMap.hashCode();
            return newHash != lastHash;
        }

        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public Konfiguration0 _update() {
            return this.hasUpdate()
                   ? new ExtMapSource(name(), map, enableNestedMap)
                   : ExtMapSource.this;
        }

    };

    @NotNull
    @Contract(pure = true)
    private Object node(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (!this.root.containsKey(key)) {
            if (enableNestedMap) {
                final List<String> parts = asList(DOT.split(key));
                Object m = this.root.get(parts.get(0));
                int i = 0;
                while (i++ < parts.size() && m instanceof Map) {
                    final String newKey = String.join(".", parts.subList(i, parts.size()));
                    final Map<?, ?> mm = (Map<?, ?>) m;
                    if (mm.containsKey(newKey))
                        return mm.get(newKey);
                    m = mm.get(parts.get(1));
                }
            }
            throw new KfgIllegalStateException(this.name(), "missing key: " + key);
        }

        if (this.root.get(key) == null)
            throw new KfgIllegalStateException(this.name(), "null key: " + key);

        return this.root.get(key);
    }

    private <T> T checkMapType(@Nullable final Q<?> required,
                               @NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Object value = node(key);
        if (!Q.matchesValue(required, value))
            throw new KfgTypeException(this.name(), key, required, value);
        @SuppressWarnings("unchecked")
        final T t = (T) value;
        return t;
    }

    ExtMapSource(@NotNull final String name,
                 @NotNull final Supplier<Map<String, ?>> mapSupplier,
                 final boolean enableNestedMap) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(mapSupplier, "mapSupplier");

        this.name = name;
        this.map = mapSupplier;
        this.root = new HashMap<>(mapSupplier.get());
        this.enableNestedMap = enableNestedMap;
        this.lastHash = this.root.hashCode();
        requireNonNull(this.map.get(), "supplied map is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Boolean bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return checkMapType(Q.BOOL, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Character char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return checkMapType(Q.CHAR, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    String string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        try {
            return checkMapType(Q.STRING, key);
        }
        catch (KfgTypeException k0) {
            try {
                return this.checkMapType(Q.CHAR, key).toString();
            }
            catch (KfgTypeException k1) {
                throw k0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Object n = node(key);
        if (n instanceof Long || n instanceof Integer ||
                n instanceof Short || n instanceof Byte)
            return ((Number) n).longValue();
        return checkMapType(Q.LONG, key);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Object n = node(key);
        if (n instanceof Long || n instanceof Integer ||
                n instanceof Short || n instanceof Byte ||
                n instanceof Double || n instanceof Float)
            return ((Number) n).doubleValue();
        return checkMapType(Q.LONG, key);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    List<?> list0(@NotNull final String key,
                  @NotNull final Q<? extends List<?>> type) {
        Objects.requireNonNull(key, "key");
        return checkMapType(type, key);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Set<?> set0(@NotNull final String key,
                @NotNull final Q<? extends Set<?>> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return checkMapType(type, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Map<?, ?> map0(@NotNull final String key,
                   @NotNull final Q<? extends Map<?, ?>> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return checkMapType(type, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object custom0(@NotNull final String key,
                   @NotNull final Q<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return checkMapType(type, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.root.get(key) == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull final String key,
                       @Nullable final Q<?> type) {
        Objects.requireNonNull(key, "key");
        return this.root.containsKey(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public KonfigurationManager0 manager() {
        return this.manager;
    }

}
