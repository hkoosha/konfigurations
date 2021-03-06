package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link #check(Kind, String)} is not very strong, regarding collection types
 * but it is ok, as {@link #has(String, Kind)} is strong enough and is called
 * before hand.
 */
@ThreadSafe
@ApiStatus.Internal
final class ExtMapSource extends Source {

    private final Supplier<Map<String, ?>> map;
    private final Map<String, ?> root;
    private final int lastHash;

    @NotNull
    private final String name;

    @NotNull
    @Contract(pure = true)
    private Object node(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (!this.root.containsKey(key))
            throw new KfgIllegalStateException(this.name(), "missing key: " + key);

        return this.root.get(key);
    }

    private <T> T check(@NotNull final Kind<?> required,
                        @NotNull final String key) {
        final Object value = node(key);

        if (!required.klass().isAssignableFrom(value.getClass()))
            throw new KfgTypeException(this.name(), key, required, value);

        @SuppressWarnings("unchecked")
        final T t = (T) value;

        return t;
    }

    ExtMapSource(@NotNull final String name,
                 @NotNull final Supplier<Map<String, ?>> mapSupplier) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(mapSupplier, "mapSupplier");

        this.name = name;
        this.map = mapSupplier;
        this.root = new HashMap<>(mapSupplier.get());
        this.lastHash = this.root.hashCode();
        Objects.requireNonNull(this.map.get(), "supplied map is null");
    }

    @Override
    @NotNull
    protected Boolean bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return check(Kind.BOOL, key);
    }

    @Override
    @NotNull
    protected Character char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return check(Kind.CHAR, key);
    }

    @Override
    @NotNull
    protected String string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        try {
            return check(Kind.STRING, key);
        }
        catch (KfgTypeException k0) {
            try {
                return this.check(Kind.CHAR, key).toString();
            }
            catch (KfgTypeException k1) {
                throw k0;
            }
        }
    }

    @Override
    @NotNull
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Object n = node(key);
        if (n instanceof Long || n instanceof Integer ||
            n instanceof Short || n instanceof Byte)
            return ((Number) n).longValue();
        return check(Kind.LONG, key);
    }

    @Override
    @NotNull
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Object n = node(key);
        if (n instanceof Long || n instanceof Integer ||
            n instanceof Short || n instanceof Byte ||
            n instanceof Double || n instanceof Float)
            return ((Number) n).doubleValue();
        return check(Kind.LONG, key);
    }

    @Override
    @NotNull
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        final List<?> asList = check(type, key);
        return Collections.unmodifiableList(asList);
    }

    @Override
    @NotNull
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        try {
            final Set<?> asSet = check(type, key);
            return Collections.unmodifiableSet(asSet);
        }
        catch (final KfgTypeException notASet) {
            final List<?> asList;
            try {
                asList = this.list0(key, type);
            }
            catch (final KfgTypeException ignore) {
                throw notASet;
            }
            final Set<?> asSet = new LinkedHashSet<>(asList);
            if (asSet.size() != asList.size())
                throw new KfgTypeException(this.name, key, type.asSet(), asList, "is a list, not a set");
            return Collections.unmodifiableSet(asSet);
        }
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return check(type, key);
    }

    @Override
    protected boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.root.containsKey(key) && this.root.get(key) == null;
    }

    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        return this.root.containsKey(key) &&
            this.root.get(key) != null &&
            type.klass().isAssignableFrom(this.root.get(key).getClass()) &&
            (!type.isCollection() || !type.getCollectionContainedKind().isParametrized()) &&
            (!type.isMap() || (type.getMapKeyKind().isParametrized() || type.getMapValueKind().isParametrized()));
    }

    @Override
    @NotNull
    public String name() {
        return this.name;
    }

    @Override
    @Contract(pure = true)
    public boolean hasUpdate() {
        final Map<String, ?> newMap = map.get();
        if (newMap == null)
            return false;
        final int newHash = newMap.hashCode();
        return newHash != lastHash;
    }

    @Contract(pure = true,
              value = "->new")
    @Override
    @NotNull
    public Source updatedCopy() {
        return new ExtMapSource(name(), map);
    }

}
