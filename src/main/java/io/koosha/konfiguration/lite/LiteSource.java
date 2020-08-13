package io.koosha.konfiguration.lite;

import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Special version of {@link LiteKonfiguration}, intended to go into a Kombiner.
 */
public abstract class LiteSource implements LiteKonfiguration {

    protected final static Pattern DOT_PATTERN = Pattern.compile("\\.");

    protected final static String DOT_PATTERN_QUOTED = Pattern.quote(".");

    @Override
    public final Boolean bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Boolean> kind = Kind.BOOL;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Object v = this.bool0(key);
        final Boolean vv = toBool(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);
        return vv;
    }

    @Override
    public final Boolean bool(@NotNull final String key,
                              final Boolean def) {
        return this.has(key, Kind.of(boolean.class).withKey(key)) ? bool(key) : def;
    }

    @Override
    public final Character char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Character> kind = Kind.CHAR;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

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
        return vv;
    }

    @Override
    public final Character char_(@NotNull final String key,
                                 final Character def) {
        return this.has(key, Kind.of(char.class).withKey(key)) ? char_(key) : def;
    }


    @Override
    public final Byte byte_(@NotNull final String key,
                            final Byte def) {
        return this.has(key, Kind.of(byte.class).withKey(key)) ? byte_(key) : def;
    }

    @Override
    public final String string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<String> kind = Kind.STRING;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Object v = this.string0(key);

        final String vv;
        try {
            vv = (String) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, kind, v);
        }

        return vv;
    }

    @Override
    public final String string(@NotNull final String key,
                               final String def) {
        return this.has(key, Kind.of(String.class).withKey(key)) ? string(key) : def;
    }


    @Override
    public final Byte byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Byte> kind = Kind.BYTE;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Number v = this.number0(key);

        final Long vv = toByte(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return vv.byteValue();
    }

    @Override
    public final Short short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Short> kind = Kind.SHORT;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Number v = this.number0(key);

        final Long vv = toShort(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return vv.shortValue();
    }

    @Override
    public final Short short_(@NotNull final String key,
                              final Short def) {
        return this.has(key, Kind.of(short.class).withKey(key)) ? short_(key) : def;
    }

    @Override
    public final Integer int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Integer> kind = Kind.INT;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Number v = this.number0(key);

        final Long vv = toInt(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return vv.intValue();
    }

    @Override
    public final Integer int_(@NotNull final String key,
                              final Integer def) {
        return this.has(key, Kind.of(int.class).withKey(key)) ? int_(key) : def;
    }

    @Override
    public final Long long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Long> kind = Kind.LONG;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Number v = this.number0(key);

        final Long vv = toLong(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return vv;
    }

    @Override
    public final Long long_(@NotNull final String key,
                            final Long def) {
        return this.has(key, Kind.of(long.class).withKey(key)) ? long_(key) : def;
    }

    @Override
    public final Float float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Float> kind = Kind.FLOAT;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Number v = this.numberDouble0(key);

        final Float vv = toFloat(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return vv;
    }

    @Override
    public final Float float_(@NotNull final String key,
                              final Float def) {
        return this.has(key, Kind.of(float.class).withKey(key)) ? float_(key) : def;
    }

    @Override
    public final Double double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final Kind<Double> kind = Kind.DOUBLE;

        if (!this.has(key, kind))
            throw new KfgMissingKeyException(this.name(), key, kind);

        if (this.isNull(key))
            return null;

        final Number v = this.numberDouble0(key);

        final Double vv = toDouble(v);
        if (vv == null)
            throw new KfgTypeException(this.name(), key, kind, v);

        return vv;
    }

    @Override
    public final Double double_(@NotNull final String key,
                                final Double def) {
        return this.has(key, Kind.of(double.class).withKey(key)) ? double_(key) : def;
    }

    @Override
    public final <U> List<U> list(@NotNull final String key,
                                  @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");

        if (!this.has(key, type.asList()))
            throw new KfgMissingKeyException(this.name(), key, type);

        if (this.isNull(key))
            return null;

        final List<?> v = this.list0(key, type);

        this.checkCollectionType(key, type, v);

        @SuppressWarnings("unchecked")
        final List<U> vv = (List<U>) v;
        return vv;
    }

    @Override
    public final <U> List<U> list(@NotNull final String key,
                                  @NotNull final Kind<U> type,
                                  final List<U> def) {
        return this.has(key, type.withKey(key)) ? list(key, type) : def;
    }

    @Override
    public final <U> Set<U> set(@NotNull final String key,
                                @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");

        if (!this.has(key, type.asSet()))
            throw new KfgMissingKeyException(this.name(), key, type);

        if (this.isNull(key))
            return null;

        final Object v = this.set0(key, type);

        final Set<?> vv;
        try {
            vv = (Set<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this.name(), key, type, v);
        }

        this.checkCollectionType(key, type, vv);

        @SuppressWarnings("unchecked")
        final Set<U> vvv = (Set<U>) vv;
        return vvv;
    }

    @Override
    public final <U> Set<U> set(@NotNull final String key,
                                @NotNull final Kind<U> type,
                                final Set<U> def) {
        return this.has(key, type.withKey(key)) ? set(key, type) : def;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <U> U custom(@NotNull final String key,
                              @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (!this.has(key, type))
            throw new KfgMissingKeyException(this.name(), key, type);

        if (this.isNull(key))
            return null;

        if (type.isBool())
            return (U) bool(key);
        if (type.isChar())
            return (U) char_(key);
        if (type.isString())
            return (U) string(key);

        if (type.isByte())
            return (U) byte_(key);
        if (type.isShort())
            return (U) short_(key);
        if (type.isInt())
            return (U) int_(key);
        if (type.isLong())
            return (U) long_(key);
        if (type.isDouble())
            return (U) double_(key);
        if (type.isFloat())
            return (U) float_(key);

        if (type.isList())
            return (U) list(key, type.getCollectionContainedKind());
        if (type.isSet())
            return (U) set(key, type.getCollectionContainedKind());

        return (U) this.custom0(key, type);
    }

    @Override
    public final <U> U custom(@NotNull final String key,
                              @NotNull final Kind<U> type,
                              final U def) {
        return has(key, type.withKey(key)) ? custom(key, type) : def;
    }


    // =========================================================================

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
     * Make sure the value is of the requested type.
     *
     * @param key        the config key whose value is being checked
     * @param neededType type asked for.
     * @param value      the value in question
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    private void checkCollectionType(@NotNull final String key,
                                     @NotNull final Kind<?> neededType,
                                     @NotNull final Object value) {
        Objects.requireNonNull(neededType, "neededType");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        if (!(value instanceof Collection))
            throw new KfgIllegalStateException(this.name(), key, neededType.asList(), value, "expecting a collection");

        for (final Object o : (Collection<?>) value)
            if (o != null && !neededType.klass().isAssignableFrom(o.getClass()))
                throw new KfgTypeException(this.name(), key, neededType, value);
    }

    @NotNull
    @Contract("_ -> fail")
    @Override
    public final LiteKonfiguration subset(@NotNull final String key) {
        return new LiteSubsetView(this.name(), this, key);
    }

}
