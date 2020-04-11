package io.koosha.konfiguration;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
public abstract class Q<TYPE> {

    @Nullable
    private final String key;

    @Nullable
    private final ParameterizedType pt;

    @NotNull
    private final Class<TYPE> klass;

    private Q(@NotNull final Class<TYPE> type) {
        Objects.requireNonNull(type, "type");
        this.key = null;
        this.pt = null;
        this.klass = type;
    }

    private Q(@Nullable final String key,
              @Nullable final ParameterizedType pt,
              @NotNull final Class<TYPE> klass) {
        Objects.requireNonNull(klass, "klass");
        this.key = key;
        this.pt = pt;
        this.klass = klass;
    }

    @SuppressWarnings("unchecked")
    protected Q() {
        final Type t = ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        checkIsClassOrParametrizedType(t, null);

        if (t instanceof ParameterizedType) {
            this.pt = (ParameterizedType) t;
            this.klass = (Class<TYPE>) this.pt.getRawType();
        }
        else {
            this.pt = null;
            this.klass = (Class<TYPE>) t;
        }

        this.key = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format("Q<>::%s",
                this.klass().getTypeName()
        );
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Q))
            return false;
        final Q<?> other = (Q<?>) o;
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.pt, other.pt)
                && Objects.equals(this.klass, other.klass);
    }

    @Override
    public final int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.key == null ? 43 : ((Object) this.key).hashCode());
        result = result * PRIME + (this.pt == null ? 43 : this.pt.hashCode());
        result = result * PRIME + this.klass.hashCode();
        return result;
    }

    @Nullable
    public final String key() {
        return this.key;
    }

    @Nullable
    public final ParameterizedType pt() {
        return this.pt;
    }

    @NotNull
    public final Class<TYPE> klass() {
        return this.klass;
    }


    public final Q<TYPE> withKey(final String key) {
        return Objects.equals(this.key, key)
                ? new Q<TYPE>(key, this.pt, this.klass) {}
                : this;
    }

    public final boolean isParametrized() {
        return this.pt != null;
    }


    @Contract(pure = true)
    final boolean matchesType(final Q<?> other) {
        if (other == null || other == this)
            return true;
        // TODO
        return other.klass().isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    final boolean matchesValue(final Object v) {
        if (v == null)
            return true;
        // TODO
        return v.getClass().isAssignableFrom(this.klass());
    }


    @Contract(pure = true)
    public static boolean matchesType(final Q<?> q0, final Q<?> q1) {
        if (q0 == null || q1 == null)
            return true;
        return q0.matchesType(q1);
    }

    @Contract(pure = true)
    public static boolean matchesValue(final Q<?> q0, final Object value) {
        if (q0 == null)
            return true;
        return q0.matchesValue(value);
    }


    /**
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a collection.
     */
    @Nullable
    public final Type getCollectionContainedType() {
        if (!this.isSet() && !this.isList())
            throw new KfgIllegalStateException(null, "type is not a set or list");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's key.
     *
     * @return type argument of the map's key represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Nullable
    public final Type getMapKeyType() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, null, Q.UNKNOWN_MAP, null, "type is not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's value.
     *
     * @return type argument of the map's value represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Nullable
    public final Type getMapValueType() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, null, Q.UNKNOWN_MAP, null, "type is not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[1];
    }


    @Contract(pure = true)
    public final boolean isBool() {
        return Boolean.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isChar() {
        return Character.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isString() {
        return String.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isByte() {
        return Byte.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isShort() {
        return Short.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isInt() {
        return Integer.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isLong() {
        return Long.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isFloat() {
        return Float.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isDouble() {
        return Double.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isSet() {
        return Set.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isList() {
        return List.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isMap() {
        return Map.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isNull() {
        return isVoid();
    }

    public final boolean isVoid() {
        //noinspection ConstantConditions
        return Void.class.isAssignableFrom(this.klass);
    }

    // =========================================================================

    /**
     * Factory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static <U> Q<U> of(@NotNull final Class<U> klass) {
        Objects.requireNonNull(klass, "klass");
        if (Q.isParametrized(klass))
            throw new KfgIllegalArgumentException(null,
                    "parametrized types are not supported, klass=" + klass);
        return new Q<U>(klass) { };
    }

    public static final Q<Boolean> BOOL = of(Boolean.class);
    public static final Q<Character> CHAR = of(Character.class);

    public static final Q<Byte> BYTE = of(Byte.class);
    public static final Q<Short> SHORT = of(Short.class);
    public static final Q<Integer> INT = of(Integer.class);
    public static final Q<Long> LONG = of(Long.class);
    public static final Q<Float> FLOAT = of(Float.class);
    public static final Q<Double> DOUBLE = of(Double.class);

    public static final Q<String> STRING = of(String.class);

    public static final Q<Map<?, ?>> UNKNOWN_MAP = of_(Map.class);
    public static final Q<Set<?>> UNKNOWN_SET = of_(Set.class);
    public static final Q<List<?>> UNKNOWN_LIST = of_(List.class);
    public static final Q<Collection<?>> UNKNOWN_COLLECTION = of_(Collection.class);

    public static final Q<Object> OBJECT = of_(Object.class);
    public static final Q<?> UNKNOWN = OBJECT;

    public static final Q<List<Byte>> LIST_BYTE = new Q<List<Byte>>() {
    };

    public static final Q<List<Short>> LIST_SHORT = new Q<List<Short>>() { };
    public static final Q<List<Integer>> LIST_INT = new Q<List<Integer>>() { };
    public static final Q<List<Long>> LIST_LONG = new Q<List<Long>>() { };
    public static final Q<List<Float>> LIST_FLOAT = new Q<List<Float>>() { };
    public static final Q<List<Double>> LIST_DOUBLE = new Q<List<Double>>() { };
    public static final Q<List<String>> LIST_STRING = new Q<List<String>>() { };

    public static final Q<Set<Byte>> SET_BYTE = new Q<Set<Byte>>() { };
    public static final Q<Set<Short>> SET_SHORT = new Q<Set<Short>>() { };
    public static final Q<Set<Integer>> SET_INT = new Q<Set<Integer>>() { };
    public static final Q<Set<Long>> SET_LONG = new Q<Set<Long>>() { };
    public static final Q<Set<Float>> SET_FLOAT = new Q<Set<Float>>() { };
    public static final Q<Set<Double>> SET_DOUBLE = new Q<Set<Double>>() { };
    public static final Q<Set<String>> SET_STRING = new Q<Set<String>>() { };

    public static final Q<Map<String, Short>> MAP_STRING__SHORT = new Q<Map<String, Short>>() { };
    public static final Q<Map<String, Integer>> MAP_STRING__INT = new Q<Map<String, Integer>>() { };
    public static final Q<Map<String, Long>> MAP_STRING__LONG = new Q<Map<String, Long>>() { };
    public static final Q<Map<String, Float>> MAP_STRING__FLOAT = new Q<Map<String, Float>>() { };
    public static final Q<Map<String, Double>> MAP_STRING__DOUBLE = new Q<Map<String, Double>>() { };
    public static final Q<Map<String, String>> MAP_STRING__STRING = new Q<Map<String, String>>() { };

    public static final Q<?> _VOID = of_(Void.class);

    // =========================================================================

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    private static <U> Q<U> of_(@NotNull final Class<?> klass) {
        Objects.requireNonNull(klass, "klass");
        return (Q<U>) of(klass);
    }

    @ApiStatus.Internal
    @Contract(pure = true)
    static void checkIsClassOrParametrizedType(@Nullable final Type p,
                                               @Nullable Type root) {
        if (root == null)
            root = p;

        if (p == null)
            return;

        if (!(p instanceof Class) && !(p instanceof ParameterizedType))
            throw new UnsupportedOperationException(
                    "only Class and ParameterizedType are supported: "
                            + root + "::" + p);

        if (!(p instanceof ParameterizedType))
            return;
        final ParameterizedType pp = (ParameterizedType) p;

        checkIsClassOrParametrizedType(root, pp.getRawType());
        for (final Type ppp : pp.getActualTypeArguments())
            checkIsClassOrParametrizedType(root, ppp);
    }

    @ApiStatus.Internal
    @Contract(pure = true)
    static boolean isParametrized(@NotNull final Class<?> p) {
        return p.getTypeParameters().length > 0 ||
                p.getSuperclass() != null && isParametrized(p.getSuperclass());
    }

}
