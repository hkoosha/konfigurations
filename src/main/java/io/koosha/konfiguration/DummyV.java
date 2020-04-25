package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.lang.String.format;

/**
 * Dummy konfig value, holding a constant konfig value with no source.
 *
 * <p>Regarding equals and hashcode: Each instance of DummyV is considered to be
 * from a different origin (in contrast to _KonfigVImpl, so only each
 * object is equal to itself only, even with same key and values.
 *
 * @param <U> type of konfig value this object holds.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_1)
public final class DummyV<U> implements K<U> {

    @NotNull
    private final String key;

    @Nullable
    private final U v;

    private final boolean exists;

    @Nullable
    private final Kind<U> type;

    private DummyV(@NotNull final String key,
                   @Nullable final U v,
                   final boolean exists,
                   @Nullable final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        this.key = key;
        this.v = v;
        this.exists = exists;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String key() {
        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Kind<U> type() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true,
            value = "null->fail;_-> this")
    @NotNull
    public K<U> deregister(@NotNull final Handle observer) {
        Objects.requireNonNull(observer, "observer");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    @NotNull
    public Handle registerSoft(@NotNull KeyObserver observer) {
        Objects.requireNonNull(observer, "observer");
        return M_1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    @NotNull
    public Handle register(@NotNull final KeyObserver observer) {
        Objects.requireNonNull(observer, "observer");
        return M_1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    @Contract(pure = true)
    public U v() {
        if (this.exists())
            return this.v;

        throw new KfgMissingKeyException(null, this.key, this.type);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public U vn() {
        final U v = this.v();

        if (v == null)
            throw new KfgMissingKeyException(null, this.key, this.type);

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean exists() {
        return this.exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String vStr;
        String keyStr = this.key;
        try {
            vStr = String.valueOf(this.v);
        }
        catch (final Throwable e) {
            vStr = "";
            keyStr = "!" + key;
        }
        return format("K[exists=%b,%s=%s]", this.exists, keyStr, vStr);
    }

    // ________________________________________________ PREDEFINED CONST VALUES

    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    public static <U> K<U> null_(@Nullable final Kind<U> type) {
        return null_(type, "");
    }

    @NotNull
    @Contract(pure = true,
            value = " _, _ -> new")
    public static <U> K<U> null_(@Nullable final Kind<U> type,
                                 @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(null, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = " _, _ -> new")
    public static <U> K<U> of(@Nullable final U u,
                              @Nullable final Kind<U> type) {
        return of(u, type, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    public static <U> K<U> of(@Nullable final U u,
                              @Nullable final Kind<U> type,
                              @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return new DummyV<>(key, u, true, type);
    }


    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    public static <U> K<U> missing(@Nullable final Kind<U> type) {
        return missing(type, "");
    }

    @NotNull
    @Contract(pure = true,
            value = " _, _ -> new")
    public static <U> K<U> missing(@Nullable final Kind<U> type,
                                   @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return new DummyV<>(key, null, false, type);
    }


    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    public static K<Boolean> false_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(false, Kind.BOOL, key);
    }

    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    public static K<Boolean> true_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(true, Kind.BOOL, key);
    }

    @NotNull
    @Contract(pure = true,
            value = " -> new")
    public static K<Boolean> false_() {
        return false_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Boolean> true_() {
        return true_("");
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Integer> mOne() {
        return mOne("");
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Integer> zero() {
        return zero("");
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Integer> one() {
        return one("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Integer> mOne(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(-1, Kind.INT, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Integer> zero(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(0, Kind.INT, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Integer> one(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(1, Kind.INT, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Boolean> bool(@Nullable final Boolean v) {
        return bool(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Boolean> bool(@Nullable final Boolean v,
                                  @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        if (v == null)
            return null_(Kind.BOOL);
        return v ? true_(key) : false_(key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Boolean> bool() {
        return missing(Kind.BOOL);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Boolean> bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.BOOL, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Character> char_(@Nullable final Character v) {
        return of(v, Kind.CHAR);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Character> char_(@Nullable final Character v,
                                     @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.CHAR, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Character> char_() {
        return char_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Character> char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.CHAR, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<String> string(@Nullable final Object v) {
        return string(String.valueOf(v), "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<String> string(@Nullable final Object v,
                                   @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(String.valueOf(v), Kind.STRING, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<String> stringMissing() {
        return stringMissing("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<String> stringMissing(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.STRING, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Byte> byte_(@Nullable final Byte v) {
        return byte_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Byte> byte_(@Nullable final Byte v,
                                @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.BYTE, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Byte> byte_() {
        return byte_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Byte> byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.BYTE, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Short> short_(@Nullable final Short v) {
        return short_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Short> short_(final Short v,
                                  @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.SHORT, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Short> short_() {
        return short_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Short> short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.SHORT, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Integer> int_(@Nullable final Integer v) {
        return int_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Integer> int_(final Integer v,
                                  @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.INT, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Integer> int_() {
        return int_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Integer> int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.INT, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Long> long_(@Nullable final Long v) {
        return long_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Long> long_(@Nullable final Long v,
                                @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.LONG, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Long> long_() {
        return long_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Long> long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.LONG, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")

    public static K<Float> float_(@Nullable final Float v) {
        return float_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Float> float_(@Nullable final Float v,
                                  @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.FLOAT, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Float> float_() {
        return float_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Float> float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.FLOAT, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Double> double_(@Nullable final Double v) {
        return double_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static K<Double> double_(@Nullable final Double v,
                                    @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return of(v, Kind.DOUBLE, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static K<Double> double_() {
        return double_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static K<Double> double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return missing(Kind.DOUBLE, key);
    }

    // =========================================================================

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<List<U>> list(@Nullable final List<U> v) {
        return list(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<List<U>> list(@Nullable final List<U> v,
                                      @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return list(v, key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<List<U>> list(@Nullable final List<U> v,
                                      @Nullable final Kind<List<U>> type) {
        return list(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    public static <U> K<List<U>> list(@Nullable final List<U> v,
                                      @NotNull final String key,
                                      @Nullable final Kind<List<U>> type) {
        Objects.requireNonNull(key, "key");
        return of(v, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static <U> K<List<U>> list() {
        return list("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<List<U>> list(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return list(key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<List<U>> list(@Nullable final Kind<List<U>> type) {
        return list("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<List<U>> list(@NotNull final String key,
                                      @Nullable final Kind<List<U>> type) {
        Objects.requireNonNull(key, "key");
        return missing(type, key);
    }

    // =========================================================================

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<Set<U>> set(@Nullable final Set<U> v) {
        return set(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<Set<U>> set(@Nullable final Set<U> v,
                                    @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return set(v, key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<Set<U>> set(@Nullable final Set<U> v,
                                    @Nullable final Kind<Set<U>> type) {
        return set(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    public static <U> K<Set<U>> set(@Nullable final Set<U> v,
                                    @NotNull final String key,
                                    @Nullable final Kind<Set<U>> type) {
        Objects.requireNonNull(key, "key");
        return of(v, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static <U> K<Set<U>> set() {
        return set("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<Set<U>> set(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return set(key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<Set<U>> set(@Nullable final Kind<Set<U>> type) {
        return set("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<Set<U>> set(@NotNull final String key,
                                    @Nullable final Kind<Set<U>> type) {
        Objects.requireNonNull(key, "key");
        return missing(type, key);
    }

    // =========================================================================

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<Collection<U>> collection(@Nullable final Collection<U> v) {
        return collection(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<Collection<U>> collection(@Nullable final Collection<U> v,
                                                  @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return collection(v, key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<Collection<U>> collection(@Nullable final Collection<U> v,
                                                  @Nullable final Kind<Collection<U>> type) {
        return collection(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    public static <U> K<Collection<U>> collection(@Nullable final Collection<U> v,
                                                  @NotNull final String key,
                                                  @Nullable final Kind<Collection<U>> type) {
        Objects.requireNonNull(key, "key");
        return of(v, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static <U> K<Collection<U>> collection() {
        return collection("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<Collection<U>> collection(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return collection(key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U> K<Collection<U>> collection(@NotNull final Kind<Collection<U>> type) {
        return collection("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U> K<Collection<U>> collection(@NotNull final String key,
                                                  @Nullable final Kind<Collection<U>> type) {
        Objects.requireNonNull(key, "key");
        return missing(type, key);
    }


    // =========================================================================

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v) {
        return map(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v,
                                          @NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return map(v, key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v,
                                          @Nullable final Kind<Map<U, V>> type) {
        return map(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    public static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v,
                                          @NotNull final String key,
                                          @Nullable final Kind<Map<U, V>> type) {
        Objects.requireNonNull(key, "key");
        return of(v, type, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    public static <U, V> K<Map<U, V>> map() {
        return map("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U, V> K<Map<U, V>> map(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return map(key, null);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public static <U, V> K<Map<U, V>> map(@Nullable final Kind<Map<U, V>> type) {
        return map("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public static <U, V> K<Map<U, V>> map(@NotNull final String key,
                                          @Nullable final Kind<Map<U, V>> type) {
        Objects.requireNonNull(key, "key");
        return missing(type, key);
    }

    private static final Handle M_1 = () -> -1L;

}

