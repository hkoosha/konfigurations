package io.koosha.konfiguration.type;

import io.koosha.konfiguration.KfgIllegalArgumentException;
import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KonfigurationFactory;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
public abstract class Kind<TYPE> implements Serializable {

    private static final long serialVersionUID = 1;

    private static final boolean ALLOW_PARAMETRIZED = true;

    @Nullable
    private final String key;

    @Nullable
    private final ParameterizedType pt;

    @NotNull
    private final Class<TYPE> klass;

    private Kind(@NotNull final Class<TYPE> type) {
        Objects.requireNonNull(type, "type");
        this.key = null;
        this.pt = null;
        this.klass = type;
    }

    private Kind(@Nullable final String key,
                 @Nullable final ParameterizedType pt,
                 @NotNull final Class<TYPE> klass) {
        Objects.requireNonNull(klass, "klass");
        this.key = key;
        this.pt = pt;
        this.klass = klass;
    }

    @SuppressWarnings("unchecked")
    protected Kind() {
        final Type t =
                ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];

        checkIsClassOrParametrizedType(t, null);

        if (t instanceof ParameterizedType) {
            if (ALLOW_PARAMETRIZED)
                throw new IllegalArgumentException("parametrized types are not supported in this version");
            this.pt = (ParameterizedType) t;
            this.klass = (Class<TYPE>) this.pt.getRawType();
        }
        else {
            this.pt = null;
            this.klass = (Class<TYPE>) t;
        }

        this.key = null;
    }

    @SuppressWarnings("unchecked")
    private Kind(final boolean dummy) {
        final Type t =
                ((ParameterizedType) this.getClass().getGenericSuperclass())
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
        return String.format(
                "Q::%s::%s::%s",
                this.klass().getTypeName(),
                this.pt == null ? "?" : this.pt,
                this.key == null ? "?" : this.key
        );
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Kind))
            return false;
        final Kind<?> other = (Kind<?>) o;
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

    @Contract(pure = true)
    @Nullable
    public final String key() {
        return this.key;
    }

    @Contract(pure = true)
    @Nullable
    public final ParameterizedType pt() {
        return this.pt;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<TYPE> klass() {
        return this.klass;
    }

    @Contract(pure = true)
    public final boolean isParametrized() {
        return this.pt != null;
    }

    @Contract(pure = true)
    @NotNull
    public final Kind<TYPE> withKey(@Nullable final String key) {
        return Objects.equals(this.key, key)
                ? this
                : new Kind<TYPE>(key, this.pt, this.klass) {
        };
    }

    // ---------------------------------

    @Contract(pure = true)
    final boolean matchesType(@Nullable final Kind<?> other) {
        if (other == null || other == this)
            return true;
        // TODO
        return Objects.equals(this.klass, other.klass)
                && Objects.equals(this.pt, other.pt);
    }

    @Contract(pure = true)
    final boolean matchesValue(@Nullable final Object v) {
        if (v == null)
            return true;
        // TODO
        return v.getClass().isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public static boolean matchesType(@Nullable final Kind<?> kind0,
                                      @Nullable final Kind<?> kind1) {
        if (kind0 == null || kind1 == null)
            return true;
        return kind0.matchesType(kind1);
    }

    @Contract(pure = true)
    public static boolean matchesValue(@Nullable final Kind<?> kind0,
                                       @Nullable final Object value) {
        if (kind0 == null || value == null)
            return true;
        return kind0.matchesValue(value);
    }

    // ---------------------------------

    /**
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a collection.
     */
    @Contract(pure = true)
    @Nullable
    public final Type getCollectionContainedType() {
        if (!this.isCollection())
            throw new KfgIllegalStateException(null, "type is not a collection");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    @Contract(pure = true)
    @Nullable
    public final Class<?> getCollectionContainedClass() {
        if (!this.isCollection())
            throw new KfgIllegalStateException(null, "type is not a collection");
        try {
            return this.pt == null ? null : (Class<?>) this.pt.getActualTypeArguments()[0];
        }
        catch (final ClassCastException cce) {
            throw new KfgIllegalStateException(null, "collection contained type is not a concrete class");
        }
    }

    public final boolean isUnknownCollection() {
        if (!this.isCollection())
            throw new KfgIllegalStateException(null, "type is not a collection");
        return this.pt == null;
    }

    /**
     * If Q represents a map, get type argument of the map's key.
     *
     * @return type argument of the map's key represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Contract(pure = true)
    @Nullable
    public final Type getMapKeyType() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, null, null, "type is not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's value.
     *
     * @return type argument of the map's value represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Contract(pure = true)
    @Nullable
    public final Type getMapValueType() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, null, null, "type is not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[1];
    }

    // ---------------------------------

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
    public final boolean isCollection() {
        return this.isList() || this.isSet();
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

    @Contract(pure = true)
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
    @Contract(value = "_ -> new", pure = true)
    public static <U> Kind<U> of(@NotNull Class<U> klass) {
        Objects.requireNonNull(klass, "klass");
        klass = box(klass);
        if (!ALLOW_PARAMETRIZED && Kind.isParametrized(klass))
            throw new KfgIllegalArgumentException(
                    null, "parametrized types are not supported, klass=" + klass);
        return new Kind<U>(klass) {
        };
    }

    @SuppressWarnings("unchecked")
    private static <U> Class<U> box(@NotNull final Class<U> klass) {
        Class<?> c;
        if (klass == byte.class)
            c = Byte.class;
        else if (klass == char.class)
            c = Character.class;
        else if (klass == short.class)
            c = Short.class;
        else if (klass == int.class)
            c = Integer.class;
        else if (klass == long.class)
            c = Long.class;
        else if (klass == float.class)
            c = Float.class;
        else if (klass == double.class)
            c = Double.class;
        else
            c = klass;
        return (Class<U>) c;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static <U> Kind<U> of_(@NotNull final Class<?> klass) {
        Objects.requireNonNull(klass, "klass");
        return new Kind(klass) {
        };
    }

    @Contract(pure = true)
    private static void checkIsClassOrParametrizedType(@Nullable final Type p,
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

    @Contract(pure = true)
    private static boolean isParametrized(@NotNull final Class<?> p) {
        return p.getTypeParameters().length > 0 ||
                p.getSuperclass() != null && isParametrized(p.getSuperclass());
    }

    // =========================================================================

    public static final Kind<Boolean> BOOL = of(Boolean.class);
    public static final Kind<Character> CHAR = of(Character.class);

    public static final Kind<Byte> BYTE = of(Byte.class);
    public static final Kind<Short> SHORT = of(Short.class);
    public static final Kind<Integer> INT = of(Integer.class);
    public static final Kind<Long> LONG = of(Long.class);
    public static final Kind<Float> FLOAT = of(Float.class);
    public static final Kind<Double> DOUBLE = of(Double.class);

    public static final Kind<String> STRING = of(String.class);

    public static final Kind<Map<?, ?>> UNKNOWN_MAP = of_(Map.class);
    public static final Kind<Set<?>> UNKNOWN_SET = of_(Set.class);
    public static final Kind<List<?>> UNKNOWN_LIST = of_(List.class);
    public static final Kind<Collection<?>> UNKNOWN_COLLECTION = of_(Collection.class);

    public static final Kind<List<Integer>> LIST_INT = new Kind<List<Integer>>(false) {
    };
    public static final Kind<Set<Integer>> SET_INT = new Kind<Set<Integer>>(false) {
    };
    public static final Kind<Map<String, Integer>> MAP_STRING__INT = new Kind<Map<String, Integer>>(false) {
    };
    public static final Kind<Map<String, String>> MAP_STRING__STRING = new Kind<Map<String, String>>(false) {
    };

    public static final Kind<?> _VOID = of_(Void.class);

}
