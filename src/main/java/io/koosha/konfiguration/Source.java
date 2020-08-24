package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Special version of {@link Konfiguration}, intended to go into a Kombiner.
 */
public abstract class Source implements Konfiguration {

    @Override
    @NotNull
    public final K<Boolean> bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Boolean> kind = Kind.BOOL;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return this.null_(key, kind);

        final Object v = this.bool0(key);
        final Boolean vv = toBool(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);
        return this.k(key, kind, vv);
    }

    @Override
    @NotNull
    public final K<Character> char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Character> kind = Kind.CHAR;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return this.null_(key, kind);

        final Object v = this.char0(key);
        char vv;
        try {
            vv = (Character) v;
        }
        catch (final ClassCastException cc0) {
            try {
                final String str = (String) v;
                if (str.length() != 1)
                    throw cc0;
                else
                    vv = str.charAt(0);
            }
            catch (final ClassCastException cce1) {
                throw new KfgTypeException(this.name(), key, kind, v);
            }
        }
        return this.k(key, kind, vv);
    }

    @Override
    @NotNull
    public final K<String> string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<String> kind = Kind.STRING;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Object v = this.string0(key);

        final String vv;
        try {
            vv = (String) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, kind, v);
        }

        return this.k(key, kind, vv);
    }

    @Override
    @NotNull
    public final K<Byte> byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Byte> kind = Kind.BYTE;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Number v = this.number0(key);

        final Long vv = toByte(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return this.k(key, kind, vv.byteValue());
    }

    @Override
    @NotNull
    public final K<Short> short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Short> kind = Kind.SHORT;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Number v = this.number0(key);

        final Long vv = toShort(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return this.k(key, kind, vv.shortValue());
    }

    @Override
    @NotNull
    public final K<Integer> int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Integer> kind = Kind.INT;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Number v = this.number0(key);

        final Long vv = toInt(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return this.k(key, kind, vv.intValue());
    }

    @Override
    @NotNull
    public final K<Long> long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Long> kind = Kind.LONG;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Number v = this.number0(key);

        final Long vv = toLong(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return this.k(key, kind, vv);
    }

    @Override
    @NotNull
    public final K<Float> float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Float> kind = Kind.FLOAT;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Number v = this.numberDouble0(key);

        final Float vv = toFloat(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return this.k(key, kind, vv);
    }

    @Override
    @NotNull
    public final K<Double> double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Double> kind = Kind.DOUBLE;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null_(key, kind);

        final Number v = this.numberDouble0(key);

        final Double vv = toDouble(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return this.k(key, kind, vv);
    }

    @Override
    @NotNull
    public final <U> K<List<U>> list(@NotNull final String key,
                                     @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");

        if (!this.has(key, type.asList()))
            throw new KfgMissingKeyException(this.name(), key, type);

        final Kind<List<U>> listKind = type.asList();

        if (this.isNull(key))
            return null_(key, listKind);

        final List<?> v = this.list0(key, type);

        this.checkCollectionType(key, type, v);

        return this.k(key, listKind, v);
    }

    @Override
    @NotNull
    public final <U> K<Set<U>> set(@NotNull final String key,
                                   @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");

        if (!this.has(key, type.asSet()))
            throw new KfgMissingKeyException(this.name(), key, type);

        final Kind<Set<U>> setKind = type.asSet();

        if (this.isNull(key))
            return null_(key, setKind);

        final Object v = this.set0(key, type);

        final Set<?> vv;
        try {
            vv = (Set<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, type, v);
        }

        this.checkCollectionType(key, type, vv);

        return this.k(key, setKind, vv);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public final <U> K<U> custom(@NotNull final String key,
                                 @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (!this.has(key, type))
            throw new KfgMissingKeyException(this.name(), key, type);

        if (this.isNull(key))
            return null_(key, type);

        if (type.isBool())
            return (K<U>) bool(key);
        if (type.isChar())
            return (K<U>) char_(key);
        if (type.isString())
            return (K<U>) string(key);

        if (type.isByte())
            return (K<U>) byte_(key);
        if (type.isShort())
            return (K<U>) short_(key);
        if (type.isInt())
            return (K<U>) int_(key);
        if (type.isLong())
            return (K<U>) long_(key);
        if (type.isDouble())
            return (K<U>) double_(key);
        if (type.isFloat())
            return (K<U>) float_(key);

        if (type.isList())
            return (K<U>) list(key, type.getCollectionContainedKind());
        if (type.isSet())
            return (K<U>) set(key, type.getCollectionContainedKind());

        return this.k(key, type, this.custom0(key, type));
    }

    // =========================================================================

    @Override
    @NotNull
    @Contract(value = "->fail")
    public final Optional<KonfigurationManager> manager() {
        throw new KfgAssertionException(this.name(), null, null, null,
            "manager() should not be called on a Source.");
    }


    protected abstract boolean isNull(@NotNull String key);

    @NotNull
    protected abstract Object bool0(@NotNull final String key);

    @NotNull
    protected abstract Object char0(@NotNull final String key);

    @NotNull
    protected abstract Object string0(@NotNull final String key);

    @NotNull
    protected abstract Number number0(@NotNull final String key);

    @NotNull
    protected abstract Number numberDouble0(@NotNull final String key);

    @NotNull
    protected abstract List<?> list0(@NotNull String key,
                                     @NotNull Kind<?> type);

    @NotNull
    protected abstract Set<?> set0(@NotNull final String key,
                                   @NotNull Kind<?> type);

    @NotNull
    protected final Set<?> listToSet(@NotNull final String key,
                                     @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final List<?> asList = this.list0(key, type);
        final Set<?> asSet = new HashSet<>(asList);
        if (asSet.size() != asList.size())
            throw new KfgTypeException(this.name(), key, type.asSet(), asList, "is a list, not a set");
        return Collections.unmodifiableSet(asSet);
    }


    @NotNull
    protected abstract Object custom0(@NotNull String key,
                                      @NotNull Kind<?> type);


    // =========================================================================

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Boolean toBool(@Nullable final Object o) {
        if (o instanceof Boolean)
            return (Boolean) o;

        if (!(o instanceof Number))
            return null;

        final Long l = toLong((Number) o);
        if (l == null)
            return null;

        //noinspection SimplifiableConditionalExpression
        return l == 0 ? false : true;
    }

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Long toByte(@Nullable final Number o) {
        return toIntegral(o, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Long toShort(@Nullable final Number o) {
        return toIntegral(o, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Long toInt(@Nullable final Number o) {
        return toIntegral(o, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Long toLong(@Nullable final Number o) {
        return toIntegral(o, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Contract(pure = true,
              value = "null, _, _ -> null")
    @Nullable
    private static Long toIntegral(@Nullable final Number o,
                                   final long min,
                                   final long max) {
        if (o == null || o instanceof Double || o instanceof Float)
            return null;

        if (o.longValue() < min || max < o.longValue())
            return null;

        return o.longValue();
    }

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Float toFloat(@Nullable final Number o) {
        if (o == null)
            return null;

        if (o.doubleValue() < Float.MIN_VALUE || Float.MAX_VALUE < o.doubleValue())
            return null;

        return o.floatValue();
    }

    @Contract(pure = true,
              value = "null -> null")
    @Nullable
    private static Double toDouble(@Nullable final Number o) {
        if (o == null)
            return null;

        return o.doubleValue();
    }


    /**
     * Handle the case where value of a key is null.
     *
     * @param key  the config key who's value is null.
     * @param type type of requested konfig.
     * @return true if it's ok to have null values.
     */
    @NotNull
    private <U> K<U> null_(@NotNull final String key,
                           @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return k(key, type, null);
    }


    /**
     * Make sure the value is of the requested type.
     *
     * @param key        the config key whose value is being checked
     * @param neededType type asked for.
     * @param value      the value in question
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    @Contract(pure = true)
    private void checkCollectionType(@NotNull final String key,
                                     @NotNull final Kind<?> neededType,
                                     @NotNull final Object value) {
        Objects.requireNonNull(neededType, "neededType");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        if (!(value instanceof Collection))
            throw new KfgIllegalStateException(this.name(), key, neededType.asList(), value,
                "expecting a collection");

        for (final Object o : (Collection<?>) value)
            if (o != null && !neededType.klass().isAssignableFrom(o.getClass()))
                throw new KfgTypeException(this.name(), key, neededType, value);
    }


    /**
     * Wrap the actual sanitized value in a  {@link K} instance.
     *
     * @param key  config key
     * @param type type holder of wanted value
     * @param <U>  generic type of wanted konfig.
     * @return the wrapped value in K.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(pure = true)
    private <U> K<U> k(@NotNull final String key,
                       @Nullable final Kind<U> type,
                       @Nullable final Object value) {
        Objects.requireNonNull(key, "key");
        return DummyV.of((U) value, type, key);
    }


    // ============================================================= UNSUPPORTED

    @NotNull
    @Contract("_ -> fail")
    @Override
    public final Konfiguration subset(@NotNull final String key) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "subset(key) shouldn't be called on classes extending="
                + getClass().getName() + ", key=" + key);
    }

    @NotNull
    @Contract("_ -> fail")
    public final Handle registerSoft(@NotNull final KeyObserver observer) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "registerSoft(observer) shouldn't be called on classes extending="
                + getClass().getName() + ", observer=" + observer);
    }

    @Contract("_ -> fail")
    @NotNull
    public final Handle register(@NotNull final KeyObserver observer) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "register(observer) shouldn't be called on classes extending="
                + getClass().getName() + ", observer=" + observer);
    }

    @Contract("_, _ -> fail")
    @Override
    @NotNull
    public final Handle registerSoft(@NotNull final KeyObserver observer,
                                     @NotNull final String key) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "registerSoft(observer, key) shouldn't be called on classes extending="
                + getClass().getName() + ", observer=" + observer
                + ", key=" + key);
    }

    @Contract("_, _ -> fail")
    @NotNull
    public final Handle register(@NotNull final KeyObserver observer,
                                 @NotNull final String key) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "register(observer, key) shouldn't be called on classes extending="
                + getClass().getName() + ", observer=" + observer
                + ", key=" + key);
    }

    @Contract("_, _ -> fail")
    @Override
    public final void deregister(@NotNull final Handle observer,
                                 @NotNull final String key) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "deregister(observer, key) shouldn't be called on classes extending="
                + getClass().getName() + ", observer=" + observer
                + ", key=" + key);
    }

    @Contract("_ -> fail")
    @Override
    public final void deregister(@NotNull final Handle observer) {
        throw new KfgAssertionException(
            this.name(), null, null, null,
            "deregister(observer) shouldn't be called on classes extending="
                + getClass().getName() + ", observer=" + observer);
    }

    @Contract(pure = true)
    @NotNull
    public abstract Source updatedCopy();

    /**
     * Indicates whether if anything is actually updated in the origin of
     * this source.
     *
     * <p>This action must <b>NOT</b> modify the instance.</p>
     *
     * <p><b>VERY VERY IMPORTANT:</b> This method is to be called only from
     * a single thread or concurrency issues will arise.</p>
     *
     * <p>Why? To check and see if it's updatable, a source might ask it's
     * origin (a web url?) to get the new content, to compare with the old
     * content, and it asks it's origin for the new content once more, to
     * actually update the values. If this method is called during
     * KonfigurationKombiner is also calling it, this might interfere and
     * lost updates may happen.</p>
     *
     * <p>To help blocking issues, update() is allowed to block the current
     * thread, and update observers will continue to work in their own
     * thread. This mechanism also helps to notify them only after when
     * <em>all</em> the combined sources are updated.</p>
     *
     * <p>NOT Thread-safe.
     *
     * @return true if the source obtained via {@link #updatedCopy()}  will
     * differ from this source.
     */
    @Contract(pure = true)
    public abstract boolean hasUpdate();

}
