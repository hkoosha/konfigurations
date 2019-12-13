package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.*;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads konfig from a plain java map.
 *
 * <p>To fulfill contract of {@link Konfiguration}, all the values put in the
 * map of konfiguration key/values supplied to the konfiguration, should be
 * immutable.
 *
 * <p>Thread safe and immutable.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ThreadSafe
@Immutable
@ApiStatus.Internal
@ApiStatus.NonExtendable
abstract class Source implements Konfiguration0 {

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Boolean> bool(@NonNull @NotNull final String key) {
        final Q<Boolean> q = Q.BOOL;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return this.null_(key, q);

        final Object v = this.bool0(key);
        final Boolean vv = toBool(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);
        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Character> char_(@NonNull @NotNull final String key) {
        final Q<Character> q = Q.CHAR;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return this.null_(key, q);

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
                throw new KfgTypeException(this.name(), key, q, v);
            }
        }
        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<String> string(@NonNull @NotNull final String key) {
        final Q<String> q = Q.STRING;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Object v = this.string0(key);

        final String vv;
        try {
            vv = (String) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, q, v);
        }

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Byte> byte_(@NonNull @NotNull final String key) {
        final Q<Byte> q = Q.BYTE;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Number v = this.number0(key);

        final Long vv = toByte(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);

        return this.k(key, q, vv.byteValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Short> short_(@NonNull @NotNull final String key) {
        final Q<Short> q = Q.SHORT;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Number v = this.number0(key);

        final Long vv = toShort(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);

        return this.k(key, q, vv.shortValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Integer> int_(@NonNull @NotNull final String key) {
        final Q<Integer> q = Q.INT;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Number v = this.number0(key);

        final Long vv = toInt(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);

        return this.k(key, q, vv.intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Long> long_(@NonNull @NotNull final String key) {
        final Q<Long> q = Q.LONG;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Number v = this.number0(key);

        final Long vv = toLong(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Float> float_(@NonNull @NotNull final String key) {
        final Q<Float> q = Q.FLOAT;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Number v = this.numberDouble0(key);

        final Float vv = toFloat(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Double> double_(@NonNull @NotNull final String key) {
        final Q<Double> q = Q.DOUBLE;

        if (this.has(key, q))
            throw new KfgAssertionException(this.name(), key, q, null, "missing key");

        if (this.isNull(key))
            return null_(key, q);

        final Number v = this.numberDouble0(key);

        final Double vv = toDouble(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, q, v);

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final <U> K<List<U>> list(@NonNull @NotNull final String key,
                                     @Nullable Q<List<U>> type) {
        if (this.has(key, type))
            throw new KfgAssertionException(this.name(), key, type, null, "missing key");

        if (type == null) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            final Q<List<U>> t = (Q) Q.UNKNOWN_LIST;
            type = t;
        }

        if (this.isNull(key))
            return null_(key, type);

        final Object v = this.list0(key, type);

        final List<?> vv;
        try {
            vv = (List<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, type, v);
        }

        this.checkCollectionType(key, type, vv);

        return this.k(key, type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final <U> K<Set<U>> set(@NonNull @NotNull final String key,
                                   @Nullable Q<Set<U>> type) {
        if (this.has(key, type))
            throw new KfgAssertionException(this.name(), key, type, null, "missing key");

        if (type == null) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            final Q<Set<U>> t = (Q) Q.UNKNOWN_SET;
            type = t;
        }

        if (this.isNull(key))
            return null_(key, type);

        final Object v = this.set0(key, type);

        final Set<?> vv;
        try {
            vv = (Set<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, type, v);
        }

        this.checkCollectionType(key, type, vv);

        return this.k(key, type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final <U, V> K<Map<U, V>> map(@NonNull @NotNull final String key,
                                         @Nullable Q<Map<U, V>> type) {
        if (this.has(key, type))
            throw new KfgAssertionException(this.name(), key, type, null, "missing key");

        if (type == null) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            final Q<Map<U, V>> t = (Q) Q.UNKNOWN_MAP;
            type = t;
        }

        if (this.isNull(key))
            return null_(key, type);

        final Object v = this.map0(key, type);

        final Map<?, ?> vv;
        try {
            vv = (Map<?, ?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, type, v);
        }

        this.checkCollectionType(key, type, vv);

        return this.k(key, type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public final <U> K<U> custom(@NonNull @NotNull final String key,
                                 @Nullable final Q<U> type) {
        if (this.has(key, type))
            throw new KfgAssertionException(this.name(), key, type, null, "missing key");

        if (this.isNull(key))
            return null_(key, type);

        if (type != null) {
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
                return (K<U>) float_(key);
            if (type.isFloat())
                return (K<U>) double_(key);

            @SuppressWarnings("rawtypes")
            final Q raw = type;
            if (type.isList())
                return (K<U>) list(key, raw);
            if (type.isMap())
                return (K<U>) map(key, raw);
            if (type.isSet())
                return (K<U>) set(key, raw);
        }

        if (type == null && !this.supportsUnTyped())
            throw new KfgAssertionException(this.name(), key, null, null, "untyped is not supported");

        final Object v = type == null
                         ? this.custom0(key)
                         : this.custom0(key, type);

        if (type != null)
            this.checkType(key, type, v);

        return this.k(key, type, v);
    }

    @NotNull
    abstract Object bool0(@NotNull final String key);

    @NotNull
    // TODO check len if from str
    abstract Object char0(@NotNull final String key);

    @NotNull
    abstract Object string0(@NotNull final String key);

    @NotNull
    abstract Number number0(@NotNull final String key);

    @NotNull
    abstract Number numberDouble0(@NotNull final String key);

    @NotNull
    abstract List<?> list0(@NotNull String key,
                           @NotNull Q<? extends List<?>> q);

    @NotNull
    abstract Set<?> set0(@NotNull final String key,
                         @NotNull Q<? extends Set<?>> type);

    @NotNull
    abstract Map<?, ?> map0(@NotNull final String key,
                            @NotNull Q<? extends Map<?, ?>> type);

    @NotNull
    Object custom0(@NotNull String key) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    abstract Object custom0(@NotNull String key,
                            @NotNull Q<?> type);

    // =========================================================================

    private void checkType0(@NonNull @NotNull final Q<?> neededType,
                            @NonNull @NotNull final String key,
                            @NonNull @NotNull final Object value) {
        if (!Q.matchesValue(neededType, value))
            throw new KfgTypeException(this.name(), key, neededType, value);
    }

    private void checkCollectionType0(@NonNull @NotNull final String key,
                                      @NonNull @NotNull final Q<?> neededType,
                                      @NonNull @NotNull final Object value) {
        if (neededType.isMap()) {
            if (!(value instanceof Map))
                throw new KfgIllegalStateException(this.name(), key, neededType, value, "expecting a map");


            for (final Object o : ((Map<?, ?>) value).values())
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this.name(), key, neededType, value);
                    }
                else if (!allowNullInCollection_(key, neededType, value))
                    throw new KfgTypeNullException(this.name(), key, neededType);

            for (final Object o : ((Map<?, ?>) value).keySet())
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this.name(), key, neededType, value);
                    }
                else if (!allowNullInCollection_(key, neededType, value))
                    throw new KfgTypeNullException(this.name(), key, neededType);
        }
        else {
            if (!(value instanceof Collection))
                throw new KfgIllegalStateException(this.name(), key, neededType, value, "expecting a collection");

            for (final Object o : (Collection<?>) value)
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this.name(), key, neededType, value);
                    }
                else if (!allowNullInCollection_(key, neededType, value))
                    throw new KfgTypeNullException(this.name(), key, neededType);
        }
    }


    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    static Boolean toBool(@Nullable final Object o) {
        if (o instanceof Boolean)
            return (Boolean) o;

        if (!(o instanceof Number))
            return null;

        final Long l = toLong((Number) o);
        if (l == null)
            return null;

        //noinspection RedundantConditionalExpression
        return l == 0 ? false : true;
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    static Long toByte(@Nullable final Number o) {
        return toIntegral(o, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    static Long toShort(@Nullable final Number o) {
        return toIntegral(o, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    static Long toInt(@Nullable final Number o) {
        return toIntegral(o, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    static Long toLong(@Nullable final Number o) {
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
    static Float toFloat(@Nullable final Number o) {
        if (o == null)
            return null;

        if (o.doubleValue() < Float.MIN_VALUE
                || Float.MAX_VALUE < o.doubleValue())
            return null;

        return o.floatValue();
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    static Double toDouble(@Nullable final Number o) {
        if (o == null)
            return null;

        return o.doubleValue();
    }


    /**
     * Determines if a call to {@link #custom(String)} and
     * {@link #has(String, Q)} is supported.
     * <p>
     * Does not affect {@link #custom(String, Q)}.
     *
     * @return true if {@link #custom(String)} is supported.
     */
    boolean supportsUnTyped() {
        return false;
    }

    abstract boolean isNull(@NonNull @NotNull String key);


    /**
     * Handle the case where value of a key is null.
     *
     * @param key  the config key who's value is null.
     * @param type type of requested konfig.
     * @return true if it's ok to have null values.
     */
    @NotNull <U> K<U> null_(@NonNull @NotNull final String key,
                            @Nullable final Q<U> type) {
        return k(key, type, null);
    }

    /**
     * Handle the case where value in a collection is null
     *
     * @param key        the config key who's collection has a null.
     * @param collection the map, list or set collection in question.
     * @param type       type of requested konfig.
     * @return true if it's ok to have null values.
     */
    boolean allowNullInCollection_(@NonNull @NotNull final String key,
                                   @Nullable final Q<?> type,
                                   @NotNull final Object collection) {
        return true;
    }

    /**
     * Make sure the value is of the requested type.
     *
     * @param neededType type asked for.
     * @param key        the config key whose value is being checked.
     * @param value      the value in question.
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    void checkType(@NonNull @NotNull final String key,
                   @NotNull @NonNull final Q<?> neededType,
                   @NotNull @NonNull final Object value) {
        checkType0(neededType, key, value);
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
    void checkCollectionType(@NotNull @NonNull final String key,
                             @NotNull @NonNull final Q<?> neededType,
                             @NotNull @NonNull final Object value) {
        checkCollectionType0(key, neededType, value);
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
    final <U> K<U> k(@NotNull @NonNull final String key,
                     @Nullable final Q<U> type,
                     @Nullable final Object value) {
        return DummyV.of((U) value, type, key);
    }

    // ============================================================= UNSUPPORTED


    /**
     * {@inheritDoc}
     */
    @NotNull
    @Contract("_ -> fail")
    @Override
    public Konfiguration subset(@NotNull String key) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> fail")
    public Handle registerSoft(@NotNull @NonNull KeyObserver observer) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> fail")
    @NotNull
    public Handle register(@NotNull @NonNull KeyObserver observer) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

    /**
     * {@inheritDoc}
     */
    @Contract("_, _ -> fail")
    @Override
    @NotNull
    public Handle registerSoft(@NotNull KeyObserver observer, @NotNull String key) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_, _ -> fail")
    @NotNull
    public Handle register(@NotNull KeyObserver observer, @NotNull String key) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

    /**
     * {@inheritDoc}
     */
    @Contract("_, _ -> fail")
    @Override
    @NotNull
    public Konfiguration deregister(@NotNull Handle observer, @NotNull String key) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

    /**
     * {@inheritDoc}
     */
    @Contract("_ -> fail")
    @Override
    @NotNull
    public Konfiguration deregister(@NotNull Handle observer) {
        throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
    }

}
